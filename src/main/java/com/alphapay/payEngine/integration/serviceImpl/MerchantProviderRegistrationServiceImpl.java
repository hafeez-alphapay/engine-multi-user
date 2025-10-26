package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.account.management.dto.request.VendorPaymentMethod;
import com.alphapay.payEngine.account.management.dto.response.PaymentMethodResponse;
import com.alphapay.payEngine.account.management.exception.AccountNotApprovedException;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.management.service.BaseUserService;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.account.merchantKyc.repository.MerchantRepository;
import com.alphapay.payEngine.account.roles.exception.UserNotFoundException;
import com.alphapay.payEngine.alphaServices.model.MerchantServiceConfigEntity;
import com.alphapay.payEngine.alphaServices.service.MerchantAlphaPayServicesService;
import com.alphapay.payEngine.config.MyFatoorahConfig;
import com.alphapay.payEngine.integration.dto.request.CreateSupplier;
import com.alphapay.payEngine.integration.dto.request.CustomizeSupplierCommissions;
import com.alphapay.payEngine.integration.exception.MerchantIsNotAllowedForGW;
import com.alphapay.payEngine.integration.model.*;
import com.alphapay.payEngine.integration.dto.request.SupplierCommission;
import com.alphapay.payEngine.integration.dto.response.BankResponse;
import com.alphapay.payEngine.integration.dto.response.ExchangeRateResponse;
import com.alphapay.payEngine.integration.exception.MerchantDocumentMissingException;
import com.alphapay.payEngine.integration.exception.MfMerchantAlreadyAssignedException;
import com.alphapay.payEngine.integration.exception.SupplierNotAssignedException;
import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import com.alphapay.payEngine.integration.repository.*;
import com.alphapay.payEngine.integration.service.MerchantProviderRegistrationService;
import com.alphapay.payEngine.integration.service.WorkflowService;
import com.alphapay.payEngine.notification.services.INotificationService;
import com.alphapay.payEngine.storage.model.MerchantDocuments;
import com.alphapay.payEngine.storage.model.RequiredDocumentsCategory;
import com.alphapay.payEngine.storage.repository.MerchantDocumentsRepository;
import com.alphapay.payEngine.storage.repository.RequiredDocumentsRepository;
import com.alphapay.payEngine.storage.serviceImpl.RemoteFileSystemStorageService;
import com.alphapay.payEngine.utilities.BeanUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.alphapay.payEngine.utilities.UtilHelper.mergeData;
@Slf4j
@Service
public class MerchantProviderRegistrationServiceImpl implements MerchantProviderRegistrationService {

    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    private MyFatoorahConfig config;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MerchantProviderRegistrationRepository merchantProviderRegistrationRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private MerchantPaymentMethodsRepository merchantPaymentMethodsRepository;
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private MerchantDocumentsRepository merchantDocumentsRepository;
    @Autowired
    private BaseUserService baseUserService;
    @Autowired
    private CustomMerchantCommissionRepository customMerchantCommissionRepository;
    @Autowired
    private RemoteFileSystemStorageService sftpService;
    @Autowired
    private RequiredDocumentsRepository requiredDocumentsRepository;
    @Autowired
    private INotificationService notificationService;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private WorkflowService workflowOrchestratorService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Value("${mbme.provider.service.id}")
    private String mbmeProviderServiceId;

    @Value("${mf.provider.service.id}")
    private String mfProviderServiceId;

    @Autowired
    private CurrencyExchangeRateRepository currencyExchangeRateRepository;

    @Autowired
    private MerchantAlphaPayServicesService merchantAlphaPayServicesService;

    @Autowired
    private MerchantRepository merchantRepository;

    @Override
    public CreateSupplier createSupplier(CreateSupplier createSupplier) throws Exception {
        UserEntity user = userRepository.findById(createSupplier.getMerchantId())
                .orElseThrow(UserNotFoundException::new);

        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mfProviderServiceId).get();
        Optional<MerchantPaymentProviderRegistration> merchantPaymentProvider = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, user.getId());

        if (merchantPaymentProvider.isPresent()) {
            throw new MfMerchantAlreadyAssignedException();
        }

//        createSupplier.setSupplierName(user.getUserDetails().getTradeNameEnglish());
        createSupplier.setMobile(user.getUserDetails().getMobileNo());
        createSupplier.setEmail(user.getUserDetails().getEmail());

//        createSupplier.setBankId(Integer.parseInt(user.getUserDetails().getBankName()));
//        createSupplier.setBankAccountHolderName(user.getUserDetails().getBankAccountName());
//        createSupplier.setBankAccount(user.getUserDetails().getAccountNumber());
//        createSupplier.setIban(user.getUserDetails().getIban());
        createSupplier.setActive(true);
//        createSupplier.setBusinessName(user.getUserDetails().getTradeNameEnglish());
        createSupplier.setDisplaySupplierDetails(true);
        createSupplier.setBusinessType(2);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("supplier", createSupplier);

        JsonNode mergedData = mergeData(dataMap);
        log.debug("supplierData::::{}", mergedData);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_create_supplier", mergedData,createSupplier.getRequestId());
        log.debug("executeWorkflowResponse::::{}", executeWorkflowResponse);
        CreateSupplier createSupplierResponse;
        try {
            createSupplierResponse = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, CreateSupplier.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        MerchantPaymentProviderRegistration entity = new MerchantPaymentProviderRegistration();
        BeanUtils.copyProperties(createSupplier, entity);
        entity.setMerchantId(user.getId());
        assert createSupplierResponse != null;
        entity.setSupplierCode(Integer.parseInt((String) createSupplierResponse.getResponseData().get("supplierCode")));
        entity.setServiceProvider(serviceProvider);
        entity.setIsDefault(true);
        entity.setStatus("InActive");
        MerchantPaymentProviderRegistration merchantProvider = merchantProviderRegistrationRepository.save(entity);
        return createSupplierResponse;
    }

    @Override
    public List<PaymentMethodResponse> getPaymentMethod() {
        List<PaymentMethodEntity> paymentMethodEntities = paymentMethodRepository.findAll();

        return paymentMethodEntities.stream()
                .map(entity -> new PaymentMethodResponse(
                        entity.getPaymentMethodId(),
                        entity.getServiceProvider().getServiceId(),
                        entity.getPaymentMethodAr(),
                        entity.getPaymentMethodEn(),
                        entity.getPaymentMethodCode()
                ))
                .toList();
    }

    @Override
    public List<BankResponse> getAllBanks() {
        List<BankEntity> banks = bankRepository.findAll();
        return banks.stream().map(bank -> new BankResponse(bank.getBankId(), bank.getBankName()))
                .collect(Collectors.toList());
    }


    public CreateSupplier uploadSupplierDocument(int supplierCode, List<MerchantDocuments> merchantDocuments, String requestId) {
        CreateSupplier createSupplierResponse = null;

        for (MerchantDocuments documents : merchantDocuments) {
            Long categoryId = documents.getDocumentCategoryId();
            RequiredDocumentsCategory requiredDocumentsCategory = requiredDocumentsRepository.findById(categoryId);

            Long fileType = 5L;
            if (requiredDocumentsCategory.getExternalFileTypeId() != null) {
                fileType = requiredDocumentsCategory.getExternalFileTypeId();
            }
            log.debug("fileType:::{}", fileType);

            byte[] fileBytes = null;
            String mediaType;
            try {
                fileBytes = sftpService.getFileFromS3(documents.getDocumentName());
                mediaType = sftpService.getFileMediaType(documents.getDocumentName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String fileName = documents.getDocumentName();
            if (mediaType == null || mediaType.isEmpty()) {
                Tika tika = new Tika();
                mediaType = tika.detect(fileBytes);
            }
            String buffer = Base64.getEncoder().encodeToString(fileBytes);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("requestId", requestId);
            dataMap.put("fileName", fileName);
            dataMap.put("mediaType", mediaType);
            dataMap.put("buffer", buffer);
            dataMap.put("fileType", fileType);
            dataMap.put("supplierCode", supplierCode);
            JsonNode mergedData = mergeData(dataMap);

            Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_upload_supplier_document", mergedData,requestId);
            try {
                createSupplierResponse = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, CreateSupplier.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return createSupplierResponse;
    }


    /**
     * @param request
     * @return
     */
    @Override
    public CreateSupplier editSupplierDetails(CreateSupplier request) {
        return null;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public CreateSupplier customizeSupplierCommissions(CustomizeSupplierCommissions request) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mfProviderServiceId).get();
        Optional<MerchantPaymentProviderRegistration> merchantPaymentProvider = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, request.getMerchantId());
        if (merchantPaymentProvider.isEmpty()) {
            throw new SupplierNotAssignedException();
        }
        CreateSupplier createSupplierResponse = null;
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("supplierCode", merchantPaymentProvider.get().getSupplierCode());
        dataMap.put("commissions", request.getSupplierCommissions());
        dataMap.put("requestId", request.getRequestId());
        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_custom_supplier_commission", mergedData,request.getRequestId());

        try {
            createSupplierResponse = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, CreateSupplier.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
    public CreateSupplier uploadSupplierDocument(CreateSupplier request) {
        MerchantEntity merchantEntity = merchantRepository.findById(request.getMerchantId()).orElseThrow(UserNotFoundException::new);
        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mfProviderServiceId).get();
        Optional<MerchantPaymentProviderRegistration> supplierDetailsEntity = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, request.getMerchantId());
        if (supplierDetailsEntity.isEmpty()) {
            throw new SupplierNotAssignedException();
        }
        List<MerchantDocuments> merchantDocuments = merchantDocumentsRepository.findByMerchantUserAccount(merchantEntity);
        if (merchantDocuments.isEmpty()) {
            throw new MerchantDocumentMissingException();
        }
        log.debug("supplierDetailsEntity::::{}", supplierDetailsEntity.get().getSupplierCode());
        return uploadSupplierDocument(supplierDetailsEntity.get().getSupplierCode(), merchantDocuments, request.getRequestId());

    }

    @Override
    public void sendEmailToMFSupplier(CreateSupplier request) {
        MerchantEntity merchantEntity = merchantRepository.findById(request.getMerchantId()).orElseThrow(UserNotFoundException::new);
        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mfProviderServiceId).get();
        Optional<MerchantPaymentProviderRegistration> supplierDetailsEntity = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, request.getMerchantId());
        try {
            List<MerchantDocuments> merchantDocuments = merchantDocumentsRepository.findByMerchantUserAccount(merchantEntity);
            if (merchantDocuments.isEmpty()) {
                throw new MerchantDocumentMissingException();
            }
            String[] msgKeys = { String.valueOf(supplierDetailsEntity.get().getSupplierCode())};
//            String[] msgKeys = {user.getUserDetails().getTradeNameEnglish(), String.valueOf(supplierDetailsEntity.get().getSupplierCode())};
//            notificationService.sendEmailNotification(request.getRequestId(), "SETUP_MF_EMAIL", msgKeys, null, "myfatoorah", Locale.ENGLISH, "004780", "Supplier Activation Request for " + user.getUserDetails().getTradeNameEnglish(), merchantDocuments);
        } catch (Throwable e) {
            log.debug("Unable to send email notification");
        }

    }

    /**
     * @param request
     * @return
     */
    @Override
    public CreateSupplier getSupplierDeposits(CreateSupplier request) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mfProviderServiceId).get();
        Optional<MerchantPaymentProviderRegistration> supplierDetailsEntity = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, request.getMerchantId());
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("supplierCode", supplierDetailsEntity.get().getSupplierCode());

        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_get_supplier_deposit", mergedData,request.getRequestId());
        try {
            return objectMapper.treeToValue((JsonNode) executeWorkflowResponse, CreateSupplier.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param request
     * @return
     */
    @Override
    public CreateSupplier GetSupplierDashboard(CreateSupplier request) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByServiceId(mfProviderServiceId).get();
        Optional<MerchantPaymentProviderRegistration> supplierDetailsEntity = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider, request.getMerchantId());
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("supplierCode", supplierDetailsEntity.get().getSupplierCode());

        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_get_supplier_dashboard", mergedData,request.getRequestId());
        try {
            return objectMapper.treeToValue((JsonNode) executeWorkflowResponse, CreateSupplier.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param request
     * @return
     */
    @Override
    public ExchangeRateResponse getMfExchangeRate(CreateSupplier request) {
        ExchangeRateResponse response = new ExchangeRateResponse();
        BeanUtility.copyProperties(request, response);

        List<CurrencyExchangeRate> savedCurrencyExchangeRate = currencyExchangeRateRepository.findByStatus("Active");
        log.debug("<------------CurrencyExchangeRate is not empty------------>{}", savedCurrencyExchangeRate);
        if (!savedCurrencyExchangeRate.isEmpty()) {
            if (savedCurrencyExchangeRate.get(0).getLastUpdated().toLocalDate().isEqual(LocalDate.now())) {
                List<CurrencyExchangeRate> currencyExchangeRate = savedCurrencyExchangeRate;
                List<ExchangeRateResponse.ExchangeRate> rateResponse = currencyExchangeRate.stream().map(rate -> {
                    ExchangeRateResponse.ExchangeRate exchangeRate = new ExchangeRateResponse.ExchangeRate();
                    exchangeRate.setCurrency(rate.getCurrency());
                    exchangeRate.setRate(rate.getRate().toPlainString());
                    return exchangeRate;
                }).collect(Collectors.toList());
                ExchangeRateResponse.ResponseData responseData = new ExchangeRateResponse.ResponseData();
                responseData.setExchangeRates(rateResponse);
                response.setResponseData(responseData);
                return response;
            }

        }
        log.debug("<------------CurrencyExchangeRate is empty------------>");
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("requestId", request.getRequestId());

        JsonNode mergedData = mergeData(dataMap);
        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_exchange_rate", mergedData,request.getRequestId());
        try {
            response = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, ExchangeRateResponse.class);


            List<CurrencyExchangeRate> currencyExchangeRate = response.getResponseData().getExchangeRates()
                    .stream()
                    .map(rate -> {
                        CurrencyExchangeRate entity = new CurrencyExchangeRate();
                        entity.setCurrency(rate.getCurrency());
                        entity.setRate(new BigDecimal(rate.getRate()));
                        return entity;
                    })
                    .collect(Collectors.toList());

            List<ExchangeRateResponse.ExchangeRate> responseDataExchangeRate = new ArrayList<>();
            for (CurrencyExchangeRate newRate : currencyExchangeRate) {
                Optional<CurrencyExchangeRate> existingRate = currencyExchangeRateRepository.findByCurrencyAndStatus(newRate.getCurrency(), "Active");
                CurrencyExchangeRate rateToSave;
                if (existingRate.isPresent()) {
                    ExchangeRateResponse.ExchangeRate responseExchangeRate = new ExchangeRateResponse.ExchangeRate();
                    rateToSave = existingRate.get();
                    rateToSave.setRate(newRate.getRate());
                    rateToSave.setLastUpdated(LocalDate.now().atStartOfDay());
                    currencyExchangeRateRepository.save(rateToSave);
                    responseExchangeRate.setCurrency(rateToSave.getCurrency());
                    responseExchangeRate.setRate(String.valueOf(rateToSave.getRate()));
                    responseDataExchangeRate.add(responseExchangeRate);
                }
            }
            response.getResponseData().setExchangeRates(responseDataExchangeRate);
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param request
     * @return
     */
    @Override
    public List<PaymentMethodResponse> getVendorPaymentMethod(VendorPaymentMethod request) {
        List<MerchantPaymentMethodsEntity> merchantPaymentMethodsEntities = null;
        MerchantServiceConfigEntity merchantServiceConfigEntity = merchantAlphaPayServicesService.validatedMerchantApiKey(request.getApiKey());
        Long merchantIdToSearchPayMethod = merchantServiceConfigEntity.getMerchantId();//vendor id
        if (request.getMerchantId() != null) {
            List<Long> subsMerchants = new ArrayList<>();
            UserEntity superMerchant = userRepository.findById(merchantIdToSearchPayMethod).orElse(null);
            if (superMerchant != null && superMerchant.getSubUsers() != null) {
                subsMerchants = superMerchant.getSubUsers().stream()
                        .map(UserEntity::getId)
                        .toList();
            }
            if (!subsMerchants.contains(request.getMerchantId())){
                throw new MerchantIsNotAllowedForGW();
            }else{
                merchantIdToSearchPayMethod = request.getMerchantId();
            }
        }

        merchantPaymentMethodsEntities = merchantPaymentMethodsRepository.findByUserId(merchantIdToSearchPayMethod);

        List<PaymentMethodEntity> paymentMethodEntities = merchantPaymentMethodsEntities.stream()
                .map(MerchantPaymentMethodsEntity::getPaymentMethod)
                .toList();

        return paymentMethodEntities.stream()
                .map(entity -> new PaymentMethodResponse(
                        entity.getPaymentMethodId(),
                        entity.getServiceProvider().getServiceId(),
                        entity.getPaymentMethodAr(),
                        entity.getPaymentMethodEn(),
                        entity.getPaymentMethodCode()
                ))
                .toList();
//        return List.of();
    }

}