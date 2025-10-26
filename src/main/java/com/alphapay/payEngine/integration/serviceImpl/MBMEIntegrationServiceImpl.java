package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.MerchantDetailsRequest;
import com.alphapay.payEngine.account.management.dto.response.MerchantDetailsResponse;
import com.alphapay.payEngine.account.management.exception.AccountNotApprovedException;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.management.service.MerchantKycService;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.account.merchantKyc.repository.MerchantRepository;
import com.alphapay.payEngine.account.roles.exception.UserNotFoundException;
import com.alphapay.payEngine.common.encryption.EncryptionService;
import com.alphapay.payEngine.common.httpclient.service.RestClientService;
import com.alphapay.payEngine.integration.dto.request.*;
import com.alphapay.payEngine.integration.dto.response.MbmeMerchantDocListResponse;
import com.alphapay.payEngine.integration.dto.response.MbmeUserLoginResponse;
import com.alphapay.payEngine.integration.exception.MBMEMerchantAlreadyAssignedException;
import com.alphapay.payEngine.integration.exception.MerchantDocumentMissingException;
import com.alphapay.payEngine.integration.exception.SupplierNotAssignedException;
import com.alphapay.payEngine.integration.model.CustomMerchantCommissionEntity;
import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;
import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import com.alphapay.payEngine.integration.repository.CustomMerchantCommissionRepository;
import com.alphapay.payEngine.integration.repository.MerchantProviderRegistrationRepository;
import com.alphapay.payEngine.integration.repository.ServiceProviderRepository;
import com.alphapay.payEngine.integration.service.MBMEIntegrationService;
import com.alphapay.payEngine.integration.service.WorkflowService;
import com.alphapay.payEngine.storage.model.MerchantDocuments;
import com.alphapay.payEngine.storage.model.RequiredDocumentsCategory;
import com.alphapay.payEngine.storage.repository.MerchantDocumentsRepository;
import com.alphapay.payEngine.storage.repository.RequiredDocumentsRepository;
import com.alphapay.payEngine.storage.serviceImpl.RemoteFileSystemStorageService;
import com.alphapay.payEngine.utilities.BeanUtility;
import com.alphapay.payEngine.utilities.PasswordGeneratorUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.alphapay.payEngine.integration.serviceImpl.WorkflowImpl.decryptMBMEResponseData;
import static com.alphapay.payEngine.utilities.UtilHelper.mergeData;

@Slf4j
@Service
@SuppressWarnings("deprecation")   // <- until Spring actually removes it
public class MBMEIntegrationServiceImpl implements MBMEIntegrationService {
    @Autowired
    RestClientService restClientService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WorkflowService workflowOrchestratorService;
    @Autowired
    private MerchantKycService merchantKycService;
    @Value("${mbme.provider.service.id}")
    private String mbmeProviderServiceId;

    @Value("${mbme.api.key.encryption}")
    private String mbmeApiKeyEncryption;
    @Value("${mbme.uploadKyc.url}")
    private String mbmeUploadKycUrl;
    @Value("${mbme.alphapay.hid}")
    private String hid;
    @Value("${mbme.alphapay.password")
    private String mbmePassword;
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;
    @Autowired
    private MerchantProviderRegistrationRepository merchantProviderRegistrationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantDocumentsRepository merchantDocumentsRepository;
    @Autowired
    private RequiredDocumentsRepository requiredDocumentsRepository;
    @Autowired
    private RemoteFileSystemStorageService sftpService;
    @Autowired
    private CustomMerchantCommissionRepository customMerchantCommissionRepository;
    @Autowired
    private EncryptionService encryptionService;

    /**
     * @param request
     * @return
     */
    @Override
    public MbmeUserLoginResponse authLogin(MbmeUserLogin request) throws Exception {
        UserEntity user = userRepository.findById(request.getMerchantId()).orElseThrow(UserNotFoundException::new);
//        String clearPassword = encryptionService.decryptPass(user.getUserDetails().getMbmePassword());
//        String email = user.getUserDetails().getMbmeUser();

//        request.setUserName(email);
//        //"M3r0P@ay3"
//        request.setPassword(clearPassword);
        request.setHid(Integer.valueOf(hid));

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("merchant", request);
        JsonNode mergedData = mergeData(dataMap);
        log.debug("mergedDataLogin:::::{}", mergedData);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_login", mergedData, request.getRequestId());
        JsonNode jsonNode = objectMapper.readTree(executeWorkflowResponse.toString());
        MbmeUserLoginResponse response = objectMapper.treeToValue(jsonNode, MbmeUserLoginResponse.class);

        BeanUtility.copyNonNullProperties(request, response);
        return response;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public MbmeMerchantRegistration registerMerchant(CreateSupplier request) throws Exception {
        String randomPassword = PasswordGeneratorUtil.generate(8);
        String mbmeEncryptedUserPassword = encryptionService.encryptPass(randomPassword);
        // Save mbme merchant password
        UserEntity user = userRepository.findById(request.getMerchantId()).orElseThrow(UserNotFoundException::new);
//        user.getUserDetails().setMbmePassword(mbmeEncryptedUserPassword);
//        user.getUserDetails().setMbmeUser(user.getUserDetails().getEmail());
        userRepository.save(user);

        MerchantDetailsRequest merchantDetailsRequest = new MerchantDetailsRequest();
        BeanUtility.copyNonNullProperties(request, merchantDetailsRequest);
        MerchantDetailsResponse merchantDetails = merchantKycService.getMerchantDetails(merchantDetailsRequest);

//        if (!merchantDetails.getManagerApproveStatus().equalsIgnoreCase("approved") || !merchantDetails.getAdminApproveStatus().equalsIgnoreCase("approved")) {
//            throw new AccountNotApprovedException();
//        }
        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mbmeProviderServiceId).get();
        Optional<MerchantPaymentProviderRegistration> merchantPaymentProvider = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, merchantDetails.getId());

        if (merchantPaymentProvider.isPresent()) {
            throw new MBMEMerchantAlreadyAssignedException();
        }

        merchantDetails.setBusinessType("company");
//        merchantDetails.setPassword(randomPassword);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("merchant", merchantDetails);
        dataMap.put("requestId", request.getRequestId());
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_create_new_merchant", mergedData, request.getRequestId());

        MbmeMerchantRegistration response = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, MbmeMerchantRegistration.class);

        MerchantPaymentProviderRegistration entity = new MerchantPaymentProviderRegistration();
        entity.setKycStatus(response.getResponseData().get("kycStatus"));
        entity.setMerchantId(request.getMerchantId());
        entity.setCommissionPercentage(BigDecimal.valueOf(0.0));
        entity.setCommissionValue(BigDecimal.valueOf(0));
        entity.setDepositTerms("N/A");
        entity.setIsDefault(false);
        entity.setStatus("InActive");
        entity.setFeeChargedBy(MerchantPaymentProviderRegistration.FeeChargedBy.Merchant);
        entity.setServiceProvider(serviceProvider);
        merchantProviderRegistrationRepository.save(entity);
        BeanUtility.copyNonNullProperties(request, response);
        return response;
    }

    @Override
    public MbmeMerchantDocListResponse getKycDocumentList(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);
        log.debug("loginResponse::::{}", loginResponse);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("hid", hid);
        dataMap.put("", loginResponse);
        JsonNode mergedData = mergeData(dataMap);
        log.debug("mergedData::::{}", mergedData);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_get_kyc_document_list", mergedData, request.getRequestId());
        log.debug("executeWorkflowResponse::{}", executeWorkflowResponse);
        MbmeMerchantDocListResponse response = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, MbmeMerchantDocListResponse.class);

        return response;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public MbmeUserLoginResponse submitKyc(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("merchant", request);
        dataMap.put("hid", hid);
        dataMap.put("", loginResponse);
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_submit_kyc_application", mergedData, request.getRequestId());

        log.debug("response2::{}", executeWorkflowResponse);
        MbmeUserLoginResponse response = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, MbmeUserLoginResponse.class);
        return response;
    }

    @Override
    public MbmeUserLoginResponse getKycStatus(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("merchant", request);
        dataMap.put("hid", hid);
        dataMap.put("", loginResponse);
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_get_kyc_status", mergedData, request.getRequestId());

        log.debug("response2::{}", executeWorkflowResponse);
        MbmeUserLoginResponse response = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, MbmeUserLoginResponse.class);


        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mbmeProviderServiceId).get();
        Optional<MerchantPaymentProviderRegistration> merchantPaymentProvider = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, request.getMerchantId());
        if (merchantPaymentProvider.isEmpty()) {
            throw new SupplierNotAssignedException();
        }

        merchantPaymentProvider.get().setKycStatus((String) response.getResponseData().get("kycStatus"));
        merchantPaymentProvider.get().setKycFeedback((String) response.getResponseData().get("isKycCompleted"));
        String isKycCompleted = (String) response.getResponseData().get("isKycCompleted");
        String newStatus = "true".equalsIgnoreCase(isKycCompleted) ? "Active" : "InActive";
        merchantPaymentProvider.get().setStatus(newStatus);
        merchantProviderRegistrationRepository.save(merchantPaymentProvider.get());

        return response;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public Object uploadMerchantDocument(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);
        MerchantEntity merchantEntity = merchantRepository.findById(request.getMerchantId()).orElseThrow(UserNotFoundException::new);
        List<MerchantDocuments> merchantDocuments = merchantDocumentsRepository.findByMerchantUserAccount(merchantEntity);
        if (merchantDocuments.isEmpty()) {
            throw new MerchantDocumentMissingException();
        }
        Object uploadDocResponse = null;
        for (MerchantDocuments documents : merchantDocuments) {
            Long categoryId = documents.getDocumentCategoryId();
            RequiredDocumentsCategory requiredDocumentsCategory = requiredDocumentsRepository.findById(categoryId);
            Long fileType = 5L;
            if (requiredDocumentsCategory.getExternalFileTypeId() != null) {
                fileType = requiredDocumentsCategory.getExternalFileTypeId();
            }
            String fileName = documents.getDocumentName();
            uploadDocResponse = uploadKycDocument(fileType, fileName, fileName, String.valueOf(loginResponse.getResponseData().get("authKey")), Long.valueOf(hid));
        }
        return uploadDocResponse;
    }

    /* --------------------------------------------------------------------- */
    /* --------------------  MBME upload main method  ----------------------- */
    /* --------------------------------------------------------------------- */

    /**
     * Upload a KYC document (downloaded from S3) to MBME.
     *
     * @param fileType numeric code MBME gave you (e.g. 20)
     * @param fileName display name shown to the merchant
     * @param s3Key    object key inside {@code bucketName}
     * @param bearer   token received from /merchant_login (authKey)
     * @param hid      ALPHA HID
     * @return MBME JSON payload (or error body)
     * @throws IOException network / IO problems
     */
    public Object uploadKycDocument(long fileType, String fileName, String s3Key, String bearer, long hid) throws IOException {

        /* 1 ── download the file (in memory) */
        byte[] fileBytes = null;
        try {
            fileBytes = sftpService.getFileFromS3(s3Key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /* 2 ── build a buffered multipart entity (Content-Length known) (Fix timeout issue)*/
        var entity = MultipartEntityBuilder.create()
                .setLaxMode()          // I added this to handle UTF-8 filenames
                .addBinaryBody("file", fileBytes,
                        ContentType.APPLICATION_OCTET_STREAM, fileName)
                .addPart("document_type",
                        new StringBody(String.valueOf(fileType), ContentType.TEXT_PLAIN))
                .addPart("document_type_name",
                        new StringBody(fileName, ContentType.TEXT_PLAIN))
                .addPart("hid",
                        new StringBody(String.valueOf(hid), ContentType.TEXT_PLAIN))
                .build();              // at this point boundary + length computed

        /* 3 ── prepare HTTP client (no chunked, length set automatically)  */
        /* 2 ── time-outs */
        //TODO take all configs to propertues
        RequestConfig cfg = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(20))
                .setResponseTimeout(Timeout.ofSeconds(90))
                .build();

        try (CloseableHttpClient http = HttpClients.custom()
                .setDefaultRequestConfig(cfg)
                .build()) {

            HttpPost post = new HttpPost(mbmeUploadKycUrl);
            post.setEntity(entity);
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
            post.setHeader(HttpHeaders.ACCEPT, "application/json");
            post.setConfig(cfg);                    // ← apply per-request config

            try (ClassicHttpResponse res = http.execute(post)) { // ← no cfg here
                String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), StandardCharsets.UTF_8) : "";

                log.debug("MBME raw response body: {}", body);
                String decrypted = decryptMBMEResponseData(body, mbmeApiKeyEncryption);
                log.debug("MBME decrypted response body: {}", decrypted);

                if (decrypted == null || !decrypted.trim().startsWith("{")) {
                    throw new IOException("Invalid MBME response: " + decrypted);
                }

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(decrypted, Object.class);
            } catch (Exception ex) {
                log.error("MBME upload error: {}", ex.getMessage());
                throw new IOException("MBME upload error: " + ex.getMessage());
            }
        }
    }

    @Override
    public CreateSupplier customizeSupplierCommissions(CustomizeSupplierCommissions request) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mbmeProviderServiceId).get();
        Optional<MerchantPaymentProviderRegistration> merchantPaymentProvider = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, request.getMerchantId());
        if (merchantPaymentProvider.isEmpty()) {
            throw new SupplierNotAssignedException();
        }
        CreateSupplier createSupplierResponse = null;

        List<CustomMerchantCommissionEntity> customMerchantCommissionEntity = new ArrayList<>();
        for (SupplierCommission cc : request.getSupplierCommissions()) {
            CustomMerchantCommissionEntity entity1 = new CustomMerchantCommissionEntity();
            BeanUtility.copyProperties(cc, entity1);
            entity1.setMerchantPaymentProviderRegistration(merchantPaymentProvider.get());
            customMerchantCommissionEntity.add(entity1);
        }
        customMerchantCommissionRepository.saveAll(customMerchantCommissionEntity);

        return createSupplierResponse;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public MbmeUserLoginResponse updateWebhookInfo(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("request", request);
        dataMap.put("hid", hid);
        dataMap.put("isWebhookEnabled", request.getRequestData().get("isWebhookEnabled"));
        dataMap.put("isSecretKeyEnabled", request.getRequestData().get("isSecretKeyEnabled"));
        dataMap.put("webhookUrl", request.getRequestData().get("webhookUrl"));
        dataMap.put("webhookSecretKey", request.getRequestData().get("webhookSecretKey"));
        dataMap.put("", loginResponse);
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_update_webhook_info", mergedData, request.getRequestId());
        log.debug("updateWebhookInfo::{}", executeWorkflowResponse);
        JsonNode jsonNode = objectMapper.readTree(executeWorkflowResponse.toString());
        MbmeUserLoginResponse response = objectMapper.treeToValue(jsonNode, MbmeUserLoginResponse.class);
        return response;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public MbmeUserLoginResponse getWebhookInfo(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("request", request);
        dataMap.put("hid", hid);
        dataMap.put("", loginResponse);
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_get_webhook_details", mergedData, request.getRequestId());
        JsonNode jsonNode = objectMapper.readTree(executeWorkflowResponse.toString());
        MbmeUserLoginResponse response = objectMapper.treeToValue(jsonNode, MbmeUserLoginResponse.class);
        log.debug("getWebhookInfo::{}", executeWorkflowResponse);
        return response;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public MbmeUserLoginResponse generateNewApiAccessKey(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("request", request);
        dataMap.put("hid", hid);
        dataMap.put("", loginResponse);
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_generate_new_api_access_key", mergedData, request.getRequestId());
        JsonNode jsonNode = objectMapper.readTree(executeWorkflowResponse.toString());
        MbmeUserLoginResponse response = objectMapper.treeToValue(jsonNode, MbmeUserLoginResponse.class);
        log.debug("generateNewApiAccessKey::{}", executeWorkflowResponse);
        return response;

    }

    /**
     * @param request
     * @return
     */
    @Override
    public MbmeUserLoginResponse getMerchantApiAccessKey(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("request", request);
        dataMap.put("hid", hid);
        dataMap.put("", loginResponse);
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_get_merchant_api_access_key_info", mergedData, request.getRequestId());

        JsonNode jsonNode = objectMapper.readTree(executeWorkflowResponse.toString());
        MbmeUserLoginResponse response = objectMapper.treeToValue(jsonNode, MbmeUserLoginResponse.class);
        log.debug("getMerchantApiAccessKey::{}", executeWorkflowResponse);
        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mbmeProviderServiceId).get();
        Optional<MerchantPaymentProviderRegistration> merchantPaymentProvider = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, request.getMerchantId());
        if (merchantPaymentProvider.isEmpty()) {
            throw new SupplierNotAssignedException();
        }
        merchantPaymentProvider.get().setMerchantApiKeyId(response.getResponseData().get("apiKeyId"));
        merchantPaymentProvider.get().setMerchantApiKey(response.getResponseData().get("apiKey"));
        merchantProviderRegistrationRepository.save(merchantPaymentProvider.get());
        return response;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public MbmeUserLoginResponse getMerchantPaymentKey(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("request", request);
        dataMap.put("hid", hid);
        dataMap.put("", loginResponse);
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_get_merchant_payment_api_key_info", mergedData, request.getRequestId());

        JsonNode jsonNode = objectMapper.readTree(executeWorkflowResponse.toString());
        MbmeUserLoginResponse response = objectMapper.treeToValue(jsonNode, MbmeUserLoginResponse.class);
        log.debug("getMerchantApiAccessKey::{}", executeWorkflowResponse);
        MbmeUserLoginResponse.ResultItem merchantPaymentKey = response.getKeys().get(0);
        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mbmeProviderServiceId).get();
        Optional<MerchantPaymentProviderRegistration> merchantPaymentProvider = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, request.getMerchantId());
        if (merchantPaymentProvider.isEmpty()) {
            throw new SupplierNotAssignedException();
        }
        merchantPaymentProvider.get().setSupplierCode(Integer.valueOf(merchantPaymentKey.getUserApiKeyId()));
        merchantPaymentProvider.get().setMerchantExternalId(merchantPaymentKey.getUserApiKeyId());
        merchantPaymentProvider.get().setMerchantExternalKey(merchantPaymentKey.getKey());
        merchantProviderRegistrationRepository.save(merchantPaymentProvider.get());
        return response;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public MbmeUserLoginResponse generateMerchantPaymentKey(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("request", request);
        dataMap.put("userRemarks", request.getRequestData().get("userRemarks"));
        dataMap.put("hid", hid);
        dataMap.put("", loginResponse);
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_generate_new_merchant_payment_api_key", mergedData, request.getRequestId());

        JsonNode jsonNode = objectMapper.readTree(executeWorkflowResponse.toString());
        MbmeUserLoginResponse response = objectMapper.treeToValue(jsonNode, MbmeUserLoginResponse.class);
        log.debug("getMerchantApiAccessKey::{}", executeWorkflowResponse);
        return response;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public MbmeUserLoginResponse updateCallbackUrl(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("request", request);
        dataMap.put("callbackUrl", request.getRequestData().get("callbackUrl"));
        dataMap.put("hid", hid);
        dataMap.put("", loginResponse);
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_update_pg_callback_url", mergedData, request.getRequestId());

        JsonNode jsonNode = objectMapper.readTree(executeWorkflowResponse.toString());
        MbmeUserLoginResponse response = objectMapper.treeToValue(jsonNode, MbmeUserLoginResponse.class);
        log.debug("getMerchantApiAccessKey::{}", executeWorkflowResponse);
        return response;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public MbmeUserLoginResponse getCallbackUrl(MbmeUserLogin request) throws Exception {
        MbmeUserLoginResponse loginResponse = authLogin(request);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("request", request);
        dataMap.put("hid", hid);
        dataMap.put("", loginResponse);
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_get_pg_callback_url", mergedData, request.getRequestId());

        JsonNode jsonNode = objectMapper.readTree(executeWorkflowResponse.toString());
        MbmeUserLoginResponse response = objectMapper.treeToValue(jsonNode, MbmeUserLoginResponse.class);
        log.debug("getMerchantApiAccessKey::{}", executeWorkflowResponse);
        return response;
    }

}
