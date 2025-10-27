package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.account.management.exception.MessageResolverService;
import com.alphapay.payEngine.account.management.service.BaseUserService;
import com.alphapay.payEngine.alphaServices.dto.request.BillPaymentRequest;
import com.alphapay.payEngine.alphaServices.dto.request.ClientBalanceCreditInAggregatorOperationDTO;
import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.model.PendingRefundProcess;
import com.alphapay.payEngine.alphaServices.repository.MerchantServicesRepository;
import com.alphapay.payEngine.alphaServices.repository.PaymentLinkEntityRepository;
import com.alphapay.payEngine.alphaServices.repository.PendingRefundProcessRepository;
import com.alphapay.payEngine.alphaServices.serviceImpl.IntegrationTokenService;
import com.alphapay.payEngine.common.bean.ErrorResponse;
import com.alphapay.payEngine.common.bean.VerifyResult;
import com.alphapay.payEngine.common.encryption.EncryptionService;
import com.alphapay.payEngine.common.httpclient.service.RestClientService;
import com.alphapay.payEngine.config.MyFatoorahConfig;
import com.alphapay.payEngine.financial.service.FinancialTransactionLedgerService;
import com.alphapay.payEngine.integration.dto.WebhookPushEvent;
import com.alphapay.payEngine.integration.dto.paymentData.InitiatePaymentRequest;
import com.alphapay.payEngine.integration.dto.paymentData.InitiatePaymentResponse;
import com.alphapay.payEngine.integration.dto.paymentData.InitiateSessionRequest;
import com.alphapay.payEngine.integration.dto.paymentData.InitiateSessionResponse;
import com.alphapay.payEngine.integration.dto.request.*;
import com.alphapay.payEngine.integration.dto.response.CustomerInfo;
import com.alphapay.payEngine.integration.dto.response.InvoiceSummaryResponse;
import com.alphapay.payEngine.integration.dto.response.MerchantInvoiceInfoResponse;
import com.alphapay.payEngine.integration.exception.AmountIsNotValidException;
import com.alphapay.payEngine.integration.exception.InvoiceLinkExpiredOrNotFoundException;
import com.alphapay.payEngine.integration.model.BackEndResponseCodeMapping;
import com.alphapay.payEngine.integration.model.MerchantPaymentMethodsEntity;
import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;
import com.alphapay.payEngine.integration.model.PaymentMethodEntity;
import com.alphapay.payEngine.integration.repository.*;
import com.alphapay.payEngine.integration.service.InitiatePaymentService;
import com.alphapay.payEngine.integration.service.WorkflowService;
import com.alphapay.payEngine.notification.services.INotificationService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import com.alphapay.payEngine.utilities.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.alphapay.payEngine.utilities.UtilHelper.mergeData;

@Slf4j
@Service
public class InitiateInitiatePaymentServiceImpl implements InitiatePaymentService {

    /* ②  Helper that never lets RestTemplate swallow the response body.
      You can move this into RestClientService if you prefer.           */
    private static final ResponseErrorHandler PASS_THRU_HANDLER = new ResponseErrorHandler() {
        @Override
        public boolean hasError(ClientHttpResponse res) throws IOException {
            return false;
        }

        @Override
        public void handleError(ClientHttpResponse res) { /* noop */ }
    };
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    RestClientService restClientService;

    @Autowired
    IntegrationTokenService tokenService;

    @Autowired
    PendingRefundProcessRepository pendingRefundProcessRepository;

    @Autowired
    ApplicationEventPublisher asyncEvent;
    //Push Update to Biller Agg
    ObjectMapper mapper = new ObjectMapper();
    @Value("${alphapay.merchant.initiate.payment.url}")
    private String initiateInvoicePaymentUrl;
    @Value("${card.autoCapture}")
    private Boolean autoCapture;
    @Value("${card.bypass3DS}")
    private Boolean bypass3DS;
    @Autowired
    private FinancialTransactionRepository financialRepository;
    @Autowired
    private FinancialTransactionLedgerService financialTransactionLedgerService;
    @Autowired
    private MerchantServicesRepository merchantServicesRepository;
    @Autowired
    private BaseUserService baseUserService;
    @Autowired
    private PaymentLinkEntityRepository paymentLinkEntityRepository;
    @Value("${hash.config.Key}")
    private String hashConfigKey;
    @Value("${hash.config.salt}")
    private String hashConfigSalt;
    @Value("${mbme.alphapay.hid}")
    private String hid;
    @Value("${mbme.alphapay.username}")
    private String alphaUserName;
    @Value("${mbme.alphapay.password}")
    private String alphaPassword;
    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MyFatoorahConfig config;
    @Autowired
    private InvoiceInitiatePaymentLogRepository invoiceInitiatePaymentLogRepository;
    @Autowired
    private MessageResolverService resolverService;
    @Autowired
    private BackEndResponseCodeMappingRepository mappingRepository;
    @Autowired
    private WorkflowService workflowOrchestratorService;
    @Autowired
    private MerchantPaymentMethodsRepository merchantPaymentMethodsRepository;
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;
    @Value("${mbme.provider.service.id}")
    private String mbmeProviderServiceId;

    @Value("${mf.provider.service.id}")
    private String mfProviderServiceId;
    @Autowired
    private MerchantProviderRegistrationRepository merchantProviderRegistrationRepository;
    @Value("${alphapay.aggregator.authorizationheader}")
    private String authorizationHeader;
    @Value("${alphapay.aggregator.engine.paymentUrl}")
    private String paymentUrl;
    @Value("${alphapay.aggregator.engine.selfTopUpUrl}")
    private String selfTopUpUrl;
    @Value("${default.tries.to.pay.invoice}")
    private String defaultTriesForInvoice;

    @Value("${providers.ids.require.approvalForRefund}")
    private String requireApprovalProvidersIdsForRefund;
    @Value("${refund.notification.emaillist}")
    private String refundNotificationEmail;

    @Autowired
    private INotificationService notificationService;

    private void assertPaymentLinkValid(Optional<PaymentLinkEntity> paymentLinkEntity) {

        if (paymentLinkEntity.isEmpty()) {
            throw new InvoiceLinkExpiredOrNotFoundException();
        }
        if (paymentLinkEntity.get().getInvoiceStatus().equals(InvoiceStatus.EXPIRED.getStatus())) {
            throw new InvoiceLinkExpiredOrNotFoundException();
        }
        if (paymentLinkEntity.get().getExpiryDateTime() != null)
            if (BeanUtility.isExpired(paymentLinkEntity.get().getExpiryDateTime()) && !paymentLinkEntity.get().getInvoiceStatus().equals(InvoiceStatus.PAID.getStatus())) {
                paymentLinkEntity.get().setInvoiceStatus(InvoiceStatus.EXPIRED.getStatus());
                paymentLinkEntityRepository.save(paymentLinkEntity.get());
                throw new InvoiceLinkExpiredOrNotFoundException();
            }

    }

    /**
     * Retrieves the summary information of a specific invoice by invoice ID.
     * Validates the payment link and returns key invoice details such as amount,
     * status, merchant information, and hash for integrity checks.
     *
     * @param request the invoice summary request containing the invoice ID
     * @return InvoiceSummaryResponse containing invoice details and merchant info
     * @throws InvoiceLinkExpiredOrNotFoundException if the invoice link is invalid, expired, or not found
     */
    @Override
    public InvoiceSummaryResponse invoiceSummary(InvoiceSummaryRequest request) {
        String[] invoiceKey = request.getInvoiceId().split("-");
        Long merchantId = Long.valueOf(invoiceKey[0]);
        Long invoiceId = Long.valueOf(invoiceKey[1]);

        Optional<PaymentLinkEntity> paymentLinkEntity = paymentLinkEntityRepository.findByInvoiceIdWithInvoiceItems(request.getInvoiceId());
        assertPaymentLinkValid(paymentLinkEntity);

        PaymentLinkEntity paymentLink = paymentLinkEntity.get();
        if (paymentLink.getId() != invoiceId || !Objects.equals(paymentLink.getMerchantUserAccount().getId(), merchantId)) {
            throw new InvoiceLinkExpiredOrNotFoundException();
        }

        String paymentId;
        if (paymentLink.getType().equals(PaymentProductType.DIRECT_PAYMENT.getName()) && !paymentLink.getPaymentId().isEmpty()) {
            paymentId = paymentLink.getPaymentId();
        } else if (paymentLink.getInvoiceStatus().equals(InvoiceStatus.ACTIVE.getStatus()) || paymentLink.getInvoiceStatus().equals(InvoiceStatus.PENDING.getStatus())) {
            paymentId = UUID.randomUUID().toString();
        } else {
            paymentId = "";
        }
        InvoiceSummaryResponse response = new InvoiceSummaryResponse();
        BeanUtility.copyProperties(request, response);
        BeanUtility.copyProperties(paymentLink, response);
        response.setInvoiceId(paymentLink.getId());
        response.setInvoice(paymentLink);
        response.setInvoiceLink(paymentLink.getInvoiceId());

        response.setInvoiceStatus(paymentLink.getInvoiceStatus());
        //avoid to return customer info in payment link in case of standard and dynamic because multiple customer can pay it
        if (!paymentLink.getType().equals(PaymentProductType.PAYMENT_LINK.getName()) &&
                !paymentLink.getType().equals(PaymentProductType.STATIC_QR_LINK.getName())) {
            response.setCustomerInfo(setCustomerInfo(paymentLink));
        }
        response.setMerchantId(merchantId);

        MerchantInvoiceInfoResponse merchantInvoiceInfo = new MerchantInvoiceInfoResponse();
        BeanUtility.copyProperties(paymentLink.getMerchantUserAccount(), merchantInvoiceInfo);
        merchantInvoiceInfo.setBusinessLogo(paymentLink.getMerchantUserAccount().getLogo());
        response.setMerchantInfo(merchantInvoiceInfo);

        double discountedPrice = (response.getDiscountedPrice() != null) ? response.getDiscountedPrice().doubleValue() : 0.0;

        String hashValue = UtilHelper.calculateRequestHash(hashConfigKey, hashConfigSalt, response.getRequestId(), response.getAmount().doubleValue(), discountedPrice, response.getPaymentLinkTitle(), response.getCurrency(), response.getInvoiceLink(), response.getMerchantId());
        response.setDiscountedPrice(BigDecimal.valueOf(discountedPrice));
        response.setHash(hashValue);
        response.setPaymentId(paymentId);
        resolverService.setAsSuccess(response);
        log.debug("response summary::::{}", response);
        return response;
    }

    private CustomerInfo setCustomerInfo(PaymentLinkEntity paymentLink) {
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setCustomerContact(paymentLink.getCustomerContact());
        customerInfo.setCountryCode(paymentLink.getCountryCode());
        customerInfo.setCustomerName(paymentLink.getCustomerName());
        customerInfo.setCustomerEmail(paymentLink.getCustomerEmail());
        return customerInfo;
    }

    /**
     * Initiates the invoice payment process by verifying the payment link,
     * updating customer details, and determining the available payment methods.
     *
     * @param request the payment initiation request containing customer and payment details
     * @return InitiatePaymentResponse with invoice and available payment method information
     * @throws InvoiceLinkExpiredOrNotFoundException if the payment link is invalid or expired
     */
    @Override
    public InitiatePaymentResponse initiateInvoicePayment(InitiatePaymentRequest request) {

        Optional<FinancialTransaction> financialTransaction = financialRepository.findFirstByPaymentIdAndTransactionTypeOrderByLastUpdatedDesc(request.getPaymentId(), PaymentStepsType.INVOICE_SUMMARY_S1.getName());
        if (financialTransaction.isEmpty()) {
            throw new InvoiceLinkExpiredOrNotFoundException();
        }

        Optional<PaymentLinkEntity> paymentLinkEntity = paymentLinkEntityRepository.findByInvoiceId(financialTransaction.get().getInvoiceLink());
        if (paymentLinkEntity.isEmpty()) {
            throw new InvoiceLinkExpiredOrNotFoundException();
        }

        PaymentLinkEntity paymentLink = paymentLinkEntity.get();
        assertPaymentLinkValid(paymentLinkEntity);
        //update customer contact
        paymentLinkEntity.get().setCustomerEmail(request.getPaymentAttributes().get("customerEmail"));
        String customerContact = request.getPaymentAttributes().get("customerContact");
        if (customerContact != null) {
            customerContact = customerContact.replaceAll("\\D", ""); // remove non-digit characters
            if (customerContact.length() > 10) {
                customerContact = customerContact.substring(customerContact.length() - 10);
            } else if (customerContact.length() <= 10 && customerContact.startsWith("0")) {
                customerContact = customerContact.substring(1);
            }
        }
        paymentLinkEntity.get().setCustomerContact(customerContact);
        paymentLinkEntity.get().setCountryCode(request.getPaymentAttributes().get("countryCode"));
        paymentLinkEntity.get().setComment(request.getPaymentAttributes().get("customerComment"));
        paymentLinkEntity.get().setPaymentId(request.getPaymentId());
        paymentLinkEntityRepository.save(paymentLink);

        InitiatePaymentResponse response = new InitiatePaymentResponse();

        //TODO:: check default payment gateway and set payment method based on it
        Optional<MerchantPaymentProviderRegistration> defaultProviderOpt = merchantProviderRegistrationRepository.findFirstByMerchantIdAndIsDefaultTrue(paymentLinkEntity.get().getMerchantUserAccount().getId());
        log.debug("defaultProviderOpt::{}", defaultProviderOpt);
        Long defaultProviderId = defaultProviderOpt.map(p -> p.getServiceProvider().getId()).orElse(-1L); // fallback to invalid ID
        log.debug("defaultProviderId::{}", defaultProviderId);

        setPaymentMethods(response, paymentLink, defaultProviderId);
        BeanUtils.copyProperties(paymentLink, response, "id");
        BeanUtils.copyProperties(request, response);
        response.setInvoiceId(paymentLink.getId());
        response.setInvoice(paymentLink);
        response.setInvoiceLink(paymentLink.getInvoiceId());
        response.setMerchantId(paymentLink.getMerchantUserAccount().getId());
        //for open amount in payment link and dynamic QR
        if (paymentLink.isOpenAmount()) {
            response.setAmount(request.getAmount());
        } else {
            response.setAmount(paymentLink.getAmount());
        }
        resolverService.setAsSuccess(response);
        return response;


    }

    private void setPaymentMethods(InitiatePaymentResponse response, PaymentLinkEntity invoiceLog, Long defaultProviderId) {
        String[] paymentMethodsCode = "ap,gp,uaecc".split(",");
        if (invoiceLog.getPaymentMethodsCode() != null && !invoiceLog.getPaymentMethodsCode().isEmpty()) {
            paymentMethodsCode = invoiceLog.getPaymentMethodsCode().split(",");
        }
        List<MerchantPaymentMethodsEntity> userPaymentMethods =
                merchantPaymentMethodsRepository.findByUserIdAndStatusOrderByDefaultProviderFirst(invoiceLog.getMerchantUserAccount().getId(), "Active", defaultProviderId, paymentMethodsCode);

        List<PaymentMethodEntity> paymentMethodEntities = userPaymentMethods.stream()
                .map(upm -> upm.getPaymentMethod()) // Extracting payment method name
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("paymentMethods", paymentMethodEntities);
        response.setResponseData(data);
    }

    @Override
    @Transactional
    public TransactionStatusResponse processStatus(PaymentStatusRequest request) {
        //As it's used in multiple location I dont want to change logic
        return processStatus(request, false);
    }

    @Override
    @Transactional
    public TransactionStatusResponse processStatus(PaymentStatusRequest request, boolean searchByBothPaymentIdAndExternalId) {
        log.trace("Payment status request received in processStatus: {}", request);
        if (request.getKeyType() == null || request.getKeyType().isBlank()) {
            request.setKeyType("PaymentId");
        }
        Optional<FinancialTransaction> payLog;
        if (searchByBothPaymentIdAndExternalId) {
            if (request.getExternalPaymentId() != null && !request.getExternalPaymentId().isBlank()) {
                log.debug("ExternalPaymentId::{}", request.getExternalPaymentId());
                payLog = financialRepository.findByExternalPaymentIdAndTransactionType(request.getExternalPaymentId(), "ExecutePaymentRequest");

            } else {
                //Serach by payment Id - only successful executions
                payLog = financialRepository.findFirstByPaymentIdAndTransactionTypeAndHttpResponseCodeOrderByLastUpdatedDesc(request.getPaymentId(), "ExecutePaymentRequest", "200");


            }
            if (!payLog.isEmpty()) {
                request.setPaymentId(payLog.get().getExternalPaymentId());
            }

        } else {
            payLog = financialRepository.findByExternalPaymentIdAndTransactionType(request.getPaymentId(), "ExecutePaymentRequest");
        }
        if (payLog.isEmpty()) {
            payLog = financialRepository.findByExternalInvoiceIdAndTransactionType(request.getPaymentId(), "ExecutePaymentRequest");
            if (payLog.isEmpty())
                throw new InvoiceLinkExpiredOrNotFoundException();
        }
        FinancialTransaction transLog = payLog.get();
        log.debug("Transaction Log for status::{}", transLog);
        Optional<PaymentLinkEntity> paymentLinkEntity = paymentLinkEntityRepository.findByInvoiceId(payLog.get().getInvoiceLink());
        TransactionStatusResponse response = null;
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("requestId", request.getRequestId());
        dataMap.put("invoiceReference", paymentLinkEntity.get().getInvoiceReference());
        dataMap.put("Key", request.getPaymentId());
        dataMap.put("KeyType", request.getKeyType());
        if (paymentLinkEntity.get().getMerchantUserAccount().getLogo() != null)
            dataMap.put("logo", paymentLinkEntity.get().getMerchantUserAccount().getLogo());
        //TODO Based on processer select WF
        JsonNode mergedData = mergeData(dataMap);
        boolean isMBME = false;
        Object executeWorkflowResponse = null;
        if (transLog.getProcessorId() != null && transLog.getProcessorId() == 2) {
           /* if(transLog.getStatus()!=null && transLog.getStatus().equals("Success"))
            {
                response= new TransactionStatusResponse();
                BeanUtils.copyProperties(transLog, response);
                return response;
            }*/
            isMBME = true;
            DateTimeFormatter mmddyyyy = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime created = transLog.getCreationTime().minusDays(1);
            LocalDateTime nowDate = LocalDateTime.now().plusDays(1);
            //Map<String, Object> dataMapMbme = new HashMap<>();
            dataMap.put("start", mmddyyyy.format(created));
            dataMap.put("end", mmddyyyy.format(nowDate));
            dataMap.put("hid", hid);
//            dataMap.put("merchant_userName", paymentLinkEntity.get().getMerchantUserAccount().getUserDetails().getMbmeUser());
//            try {
//                dataMap.put("merchant_password", encryptionService.decryptPass(paymentLinkEntity.get().getMerchantUserAccount().getUserDetails().getMbmePassword()));
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
            dataMap.put("tran", transLog.getPaymentId());
            //TODO Supplier Code
            dataMap.put("supplierCode", paymentLinkEntity.get().getMerchantUserAccount().getId());
//            dataMap.put("supplierName", paymentLinkEntity.get().getMerchantUserAccount().getUserDetails().getTradeNameEnglish());

            JsonNode mergedDataMbme = mergeData(dataMap);

            String mbmeToken=tokenService.getValidToken("mbme_login");
            dataMap.put("apiToken", mbmeToken);
            //Object executeWorkflowLogin = workflowOrchestratorService.executeWorkflow("mbme_login", mergedDataMbme,request.getRequestId());
            //log.debug(">>>>> login {}", executeWorkflowLogin);
            //dataMap.put("response", executeWorkflowLogin);
            mergedDataMbme = mergeData(dataMap);
            executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_transaction_status", mergedDataMbme,request.getRequestId());
            log.debug(">>>>> executeWorkflowResponse {}", executeWorkflowResponse);


        } else {
            executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_transaction_status", mergedData,request.getRequestId());
            log.debug(">>>>> executeWorkflowResponse {}", executeWorkflowResponse);

        }
        try {
            response = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, TransactionStatusResponse.class);

            if (isMBME) {
                log.debug("Response Data from mbem {}", response);

            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        assert response != null;
        if (response.getResponseData() != null) {
            if (isMBME) {
                String actualStatus = response.getStatus();
                PaymentLinkEntity link = paymentLinkEntity.get();
                response.getResponseData().setCustomerName(link.getCustomerName());
                response.getResponseData().setCustomerEmail(link.getCustomerEmail());
                response.getResponseData().setCustomerMobile(link.getCustomerContact());
                if (link.getExpiryDateTime() != null) {
                    String isoUtc = DateTimeFormatter
                            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                            .withZone(ZoneOffset.UTC)
                            .format(link.getExpiryDateTime().toInstant());
                    response.getResponseData().setExpiryDate(isoUtc);
                }
                response.getResponseData().setComments(link.getComment());
                /*response.getResponseData().setSuppliers(link.g().stream()
                        .map(supplier -> {
                            TransactionStatusResponse.ResponseData.Supplier supplierInfo = new TransactionStatusResponse.ResponseData.Supplier();
                            supplierInfo.setSupplierCode(supplier.getSupplierCode());
                            supplierInfo.setDepositShare(supplier.getDepositShare());
                            return supplierInfo;
                        })
                        .collect(Collectors.toList()));*/
                if (paymentLinkEntity.get().getInvoiceItems() != null && paymentLinkEntity.get().getInvoiceItems().size() > 0) {
                    response.getResponseData().setInvoiceItems(paymentLinkEntity.get().getInvoiceItems().stream()
                            .map(invoiceItemEntity -> {
                                TransactionStatusResponse.ResponseData.InvoiceItem invoiceItem = new TransactionStatusResponse.ResponseData.InvoiceItem();
                                invoiceItem.setItemName(invoiceItemEntity.getName());  // Map 'name' to 'itemName'
                                invoiceItem.setQuantity(invoiceItemEntity.getQuantity());  // Map 'quantity'
                                invoiceItem.setUnitPrice(invoiceItemEntity.getUnitPrice());  // Map 'unitPrice'
                                return invoiceItem;
                            })
                            .collect(Collectors.toList()));
                }

                //BeanUtility.copyProperties(transLog, response.getResponseData());
                //BeanUtility.copyProperties(paymentLinkEntity.get(), response.getResponseData());

                TransactionStatusResponse.ResponseData.InvoiceTransaction invoiceTransaction = new TransactionStatusResponse.ResponseData.InvoiceTransaction();
                //BeanUtility.copyProperties(transLog, invoiceTransaction);
                //BeanUtility.copyProperties(paymentLinkEntity.get(), invoiceTransaction);

                invoiceTransaction.setTransactionStatus(actualStatus);
                response.setStatus(actualStatus);
                invoiceTransaction.setPaymentId(request.getPaymentId());
                response.getResponseData().setInvoiceId(actualStatus);

                //response.getResponseData().getInvoiceTransactions().add(invoiceTransaction);

            }
            transLog.setIncomingPaymentAttributes(response.getResponseData().toFlatMap());


            if (response.getResponseData().getInvoiceTransactions() != null && !response.getResponseData().getInvoiceTransactions().isEmpty()) {
                TransactionStatusResponse.ResponseData.InvoiceTransaction invoiceTransactions = response.getResponseData().getInvoiceTransactions().get(0);
                if (!response.getResponseData().getSuppliers().isEmpty()) {
                    TransactionStatusResponse.ResponseData.Supplier supplier = response.getResponseData().getSuppliers().get(0);
                    if (supplier.getDepositShare() != null) {
                        transLog.setDepositShare(supplier.getDepositShare());
                        transLog.setTotalCharges(invoiceTransactions.getTransactionValue().subtract(supplier.getDepositShare()));
                    }
                }
                transLog.setTransactionStatus(invoiceTransactions.getTransactionStatus());
                transLog.setTransactionTime(invoiceTransactions.getTransactionDate());
                transLog.setTransactionId(invoiceTransactions.getTransactionId());
                transLog.setPaidCurrency(invoiceTransactions.getPaidCurrency());
                transLog.setPaidCurrencyValue(invoiceTransactions.getPaidCurrencyValue());
                transLog.setCountry(invoiceTransactions.getCountry());
                //transLog2.setExternalPaymentId(invoiceTransactions.getPaymentId());
                transLog.setExternalInvoiceId(response.getResponseData().getInvoiceId());
                transLog.setVat(invoiceTransactions.getVatAmount());
                transLog.setCustomerServiceCharge(invoiceTransactions.getCustomerServiceCharge());
                transLog.setPaymentMethod(invoiceTransactions.getPaymentGateway());
                transLog.setInvoiceStatus(response.getResponseData().getInvoiceStatus());
                if(transLog.getCardNumber() == null  ){//in case of apple pay
                    transLog.setCardNumber(response.getResponseData().getInvoiceTransactions().get(0).getCardNumber());
                }
                if (invoiceTransactions.getTransactionStatus().equalsIgnoreCase("Success")) {
                    log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                }

                if (invoiceTransactions.getTransactionStatus().equalsIgnoreCase("succss") || invoiceTransactions.getTransactionStatus().equalsIgnoreCase("success")) {
                    transLog.setTransactionStatus("Success");
                    transLog.setResponseMessage("Payment Completed Successfully");
                    log.debug(">>>>>>>>>>>>>>>>>>>>>");
                    log.debug("paymentLinkEntity::{}", paymentLinkEntity.isPresent());
                    if (PaymentProductType.PAYMENT_LINK.getName().equals(paymentLinkEntity.get().getType())) {
                        int successfulAttempts = paymentLinkEntity.get().getSuccessfulAttempts() + 1;
                        paymentLinkEntity.get().setSuccessfulAttempts(successfulAttempts);
                    } else {
                        paymentLinkEntity.get().setExternalPaymentId(request.getPaymentId());
                        paymentLinkEntity.get().setSuccessfulAttempts(1);
                    }
                    response.getResponseData().setInvoiceId(paymentLinkEntity.get().getInvoiceId());
                    response.getResponseData().setInvoiceTitle(paymentLinkEntity.get().getPaymentLinkTitle());
                    response.getResponseData().setInvoiceDescription(paymentLinkEntity.get().getDescription());
                    response.getResponseData().setInvoiceReference(paymentLinkEntity.get().getInvoiceReference());
                    response.getResponseData().setSignatureRequired(paymentLinkEntity.get().isSignatureRequired());
                    response.getResponseData().setSignatureUrl(paymentLinkEntity.get().getSignatureUrl());
                    response.getResponseData().setAdditionalInputs(paymentLinkEntity.get().getAdditionalInputs());
                } else {
                    if (paymentLinkEntity.get().getTotalPaymentAttempts() >= Integer.valueOf(defaultTriesForInvoice) && paymentLinkEntity.get().getType().equals(PaymentProductType.INVOICE_LINK.getName())) {
                        paymentLinkEntity.get().setInvoiceStatus(InvoiceStatus.FAILED.getStatus());
                        paymentLinkEntity.get().setExternalPaymentId(request.getPaymentId());
                    }
                }

                if ("Success".equals(transLog.getTransactionStatus()) && paymentLinkEntity.isPresent()) {
                    PaymentLinkEntity payLinkItem = paymentLinkEntity.get();
                    pushCredit(payLinkItem, transLog);
                    if (payLinkItem.getAdditionalOutputs() != null) {
                        response.getResponseData().setAdditionalOutputs(payLinkItem.getAdditionalOutputs());
                    }
                }

                if (invoiceTransactions.getTransactionStatus().equalsIgnoreCase("InProgress")) {
                    transLog.setTransactionStatus("Cancelled");
                    transLog.setResponseMessage("The payment has not been completed and the customer is still in the process of entering the verification number or has cancelled the payment process.");
                }

                if (invoiceTransactions.getErrorCode() != null)
                    transLog.setResponseMessage(invoiceTransactions.getErrorMessage());

                if (invoiceTransactions.getErrorCode() != null && !invoiceTransactions.getErrorCode().isEmpty()) {
                    String externalResponseCode = invoiceTransactions.getErrorCode();
                    log.debug("getErrorCode::{}", externalResponseCode);
                    List<BackEndResponseCodeMapping> responseCodeMapping = mappingRepository.findByExternalResponseCode(externalResponseCode);
                    if (!responseCodeMapping.isEmpty()) {
                        log.debug("responseCodeMapping::{}", responseCodeMapping.get(0).getAppResponseMessage());
                        transLog.setResponseMessage(responseCodeMapping.get(0).getAppResponseMessage());
                        invoiceTransactions.setErrorCode(responseCodeMapping.get(0).getAppResponseCode());
                        invoiceTransactions.setErrorMessage(responseCodeMapping.get(0).getAppResponseMessage());
                    }
                }
                if (paymentLinkEntity.isPresent()) {
                    response.getResponseData().setInvoiceId(paymentLinkEntity.get().getInvoiceId());
                    response.getResponseData().setInvoiceValue(paymentLinkEntity.get().getAmount().doubleValue());
                    response.getResponseData().setInvoiceDisplayValue(paymentLinkEntity.get().getAmount().toString() + " " + paymentLinkEntity.get().getCurrency());
                    // Update external payment ID and status of the payment link if it is not type of( STANDARD  or DYNAMIC_LINK).
                    // This ensures only  invoice,gateway,direct  payment are updated to reflect the transaction details post-payment execution.
                    if (!PaymentProductType.STATIC_QR_LINK.getName().equals(paymentLinkEntity.get().getType()) &&
                            !PaymentProductType.PAYMENT_LINK.getName().equals(paymentLinkEntity.get().getType())) {
                        if (request.getKeyType().equals("PaymentId") &&
                                response.getResponseData().getInvoiceStatus().equals(InvoiceStatus.PAID.getStatus())) {
                            paymentLinkEntity.get().setExternalPaymentId(request.getPaymentId());
                        }
                        if (response.getResponseData().getInvoiceStatus().equals(InvoiceStatus.PAID.getStatus())) {
                            paymentLinkEntity.get().setInvoiceStatus(response.getResponseData().getInvoiceStatus());
                        }
                    }


                    if (paymentLinkEntity.get().getCallBackUrl() != null) {
                        String redirectUrl = paymentLinkEntity.get().getCallBackUrl() + "?paymentId=" + request.getPaymentId() + "&Id=" + paymentLinkEntity.get().getInvoiceId() + "&alphaPaymentId=" + paymentLinkEntity.get().getPaymentId();
                        response.setRedirectUrl(redirectUrl);
                        response.getResponseData().setInvoiceId(paymentLinkEntity.get().getInvoiceId());
                     }
                }
            }
           safeSave(transLog);
        }

        //if(response.getResponseData()!=null && !response.getResponseData().getInvoiceStatus().equals("Pending"))
        {
          //  webhookPusher.pushWebHook(
            //        response, paymentLinkEntity.get(),null);
            asyncEvent.publishEvent(new WebhookPushEvent(response, paymentLinkEntity.get(), null));

            // if not pending - push status

        }
        return response;
    }

    @Transactional
    public void safeSave(FinancialTransaction transLog) {
        // If it's a new entity, a single save is fine (no versioning yet)
        if (transLog.getId() == null) {
            financialTransactionLedgerService.saveAndFlush(transLog);
            return;
        }

        int attempts = 0;
        while (true) {
            try {
                // Try saving what we currently have (may be detached with stale version)
                financialTransactionLedgerService.saveAndFlush(transLog);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (++attempts >= 3) throw e;

                // 1) Reload the latest version from DB
                FinancialTransaction fresh = financialRepository
                        .findById(transLog.getId())
                        .orElseThrow();

                // 2) Re-apply caller's intent (non-null fields) onto the fresh entity,
                //    but never touch id/version/audit timestamps
                copyNonNullPropsExceptImmutable(transLog, fresh,
                        "id", "version", "creationTime", "lastUpdated");

                // 3) Next loop will try saving the refreshed+reapplied entity
                transLog = fresh;
            }
        }
    }

    /** Copy only non-null properties from `src` to `dest`, ignoring the provided property names. */
    private static void copyNonNullPropsExceptImmutable(Object src, Object dest, String... propsToIgnore) {
        String[] nullProps = getNullPropertyNames(src);
        // build the effective ignore list = null props + explicit ignores
        Set<String> ignore = new HashSet<>(Set.of(nullProps));
        for (String p : propsToIgnore) ignore.add(p);
        BeanUtils.copyProperties(src, dest, ignore.toArray(String[]::new));
    }

    /** Returns names of all properties that are null on `source` (so we don't overwrite with nulls). */
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : src.getPropertyDescriptors()) {
            Object val = src.getPropertyValue(pd.getName());
            if (val == null) emptyNames.add(pd.getName());
        }
        return emptyNames.toArray(String[]::new);
    }




    @Override
    public FinancialTransaction makeRefund(PortalRefundRequest request) {
        log.debug("PortalRefundRequest------------>{}",request);
        Optional<FinancialTransaction> transactionLog = financialRepository
                .findByPaymentIdAndTransactionTypeAndInvoiceStatusAndCurrency(request.getPaymentId(),
                        "ExecutePaymentRequest", "Paid", request.getCurrency());
        if (transactionLog.isEmpty()) {
            throw new InvoiceLinkExpiredOrNotFoundException();
        }
        BigDecimal exchangeRate = transactionLog.get().getExchangeRate();
        BigDecimal refundAmountSourceCurrency = request.getAmount();
        BigDecimal refundAmountBaseCurrency = refundAmountSourceCurrency.divide(exchangeRate, 2, RoundingMode.HALF_UP);
        BigDecimal totalAmountSourceCurrency = transactionLog.get().getAmount();
        String sourceCurrency = transactionLog.get().getCurrency();
        String baseCurrency = transactionLog.get().getPaidCurrency();

        if (refundAmountBaseCurrency.compareTo(new BigDecimal("0.1")) < 0) {
            throw new AmountIsNotValidException("Amount should be greater than "
                    + new BigDecimal("0.1").multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP) + " "
                    + sourceCurrency);
        }

        if (totalAmountSourceCurrency.compareTo(refundAmountSourceCurrency) < 0) {
            throw new AmountIsNotValidException(
                    "Max Amount to refund is " + totalAmountSourceCurrency + " " + sourceCurrency);
        }

        List<FinancialTransaction> directRefundHistory = financialRepository
                .findAllByPaymentIdAndTransactionTypeAndTransactionStatus(request.getPaymentId(),
                        "DirectPaymentRefundRequest", "Success");

        List<FinancialTransaction> portalRefundHistory = financialRepository
                .findAllByPaymentIdAndTransactionTypeAndTransactionStatus(request.getPaymentId(),
                        "PortalRefundRequest", "Success");
        new BigDecimal("0.0");
        BigDecimal totalDirectRefundAmount;
        new BigDecimal("0.0");
        BigDecimal totalPortalRefundAmount;

        totalDirectRefundAmount = directRefundHistory.stream()
                .map(FinancialTransaction::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalPortalRefundAmount = portalRefundHistory.stream()
                .map(FinancialTransaction::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefundedAmount = totalDirectRefundAmount.add(totalPortalRefundAmount);

        // Check if refundAmount + totalRefundedAmount > totalAmount
        if (totalRefundedAmount.add(refundAmountSourceCurrency).compareTo(totalAmountSourceCurrency) > 0) {
            throw new AmountIsNotValidException(
                    "Refund exceeds allowable amount. Already refunded: " + totalRefundedAmount + ", Requested: "
                            + refundAmountSourceCurrency + ", Maximum: " + totalAmountSourceCurrency);
        }
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setKey(request.getPaymentId());
        refundRequest.setKeyType("PaymentId");
        refundRequest.setComment(request.getComment());
        refundRequest.setSupplierDeductedAmount(refundAmountBaseCurrency);
        refundRequest.setRequestId(request.getRequestId());
        List<Long> providerIds = Arrays.stream(requireApprovalProvidersIdsForRefund.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .toList();
        FinancialTransaction refundStatus = null;
        refundRequest.setMerchantId(transactionLog.get().getMerchantId());
        refundRequest.setMerchantName(transactionLog.get().getInvoice().getBusinessName());
        refundRequest.setProcessorId(transactionLog.get().getProcessorId());
        if(providerIds.contains(transactionLog.get().getProcessorId())){
             refundStatus = initiateRefundForApproval(refundRequest);

            String[] msgKeys = {
                    refundAmountSourceCurrency+" "+sourceCurrency,
                    refundAmountBaseCurrency +" "+baseCurrency,
                    refundRequest.getMerchantName(),
                    request.getPaymentId(),
                    totalPortalRefundAmount.toString(),
                    totalDirectRefundAmount.toString(),
                    totalRefundedAmount.toString()
            };

            notificationService.sendEmailNotification(request.getRequestId(), "FINANCE_MANAGER_NEW_REFUND_REQUEST_EMAIL", msgKeys, refundNotificationEmail, "", Locale.ENGLISH, httpServletRequest.getAttribute("applicationId") + "", "email_refund_request_notification.html");

        }
        else {
             refundStatus = processRefund(refundRequest);
        }
            refundStatus.setAmount(refundAmountSourceCurrency);
            refundStatus.setCurrency(sourceCurrency);
            refundStatus.setRefundAmount(refundAmountBaseCurrency);
            refundStatus.setRefundCurrency(baseCurrency);
            refundStatus.setPaidCurrencyValue(refundAmountBaseCurrency);
            refundStatus.setPaidCurrency(baseCurrency);
            refundStatus.setCardNumber(transactionLog.get().getCardNumber());
            refundStatus.setExchangeRate(transactionLog.get().getExchangeRate());
            return refundStatus;

    }
        @Override
        public FinancialTransaction processRefund(RefundRequest request) {
        Optional<FinancialTransaction> transactionLog = financialRepository.findByExternalPaymentIdAndTransactionTypeAndInvoiceStatus(request.getKey(), "ExecutePaymentRequest", "Paid");
        if (transactionLog.isEmpty()) {
            transactionLog = financialRepository.findByPaymentIdAndTransactionTypeAndInvoiceStatus(request.getKey(), "ExecutePaymentRequest", "Paid");
            if (transactionLog.isEmpty()) {
                throw new InvoiceLinkExpiredOrNotFoundException();
            }
        }
        String serviceProviderServiceId = "NA";
        if (transactionLog.get().getProcessorId() != null) {
            if (transactionLog.get().getProcessorId() == 2) {
                serviceProviderServiceId = mbmeProviderServiceId;
            } else if (transactionLog.get().getProcessorId() == 1) {
                serviceProviderServiceId = mfProviderServiceId;
            }
        }
        Optional<MerchantPaymentProviderRegistration> merchantPaymentProvider =
                merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProviderRepository.findByServiceId(serviceProviderServiceId).get(), transactionLog.get().getMerchantId());
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("requestId", request.getRequestId());
        dataMap.put("merchantId", transactionLog.get().getMerchantId());
        dataMap.put("invoiceLink", transactionLog.get().getInvoiceLink());
        dataMap.put("paymentId", transactionLog.get().getPaymentId());
        dataMap.put("key", transactionLog.get().getExternalPaymentId());
        dataMap.put("keyType", request.getKeyType());
        dataMap.put("vendorDeductAmount", 0);
        dataMap.put("comment", request.getComment());
        dataMap.put("supplierCode", merchantPaymentProvider.get().getSupplierCode());
        dataMap.put("supplierDeductedAmount", request.getSupplierDeductedAmount());
        dataMap.put("supplierDeductedAmountString", request.getSupplierDeductedAmount().toPlainString());

        JsonNode mergedData = mergeData(dataMap);

        Object executeWorkflowResponse = null;
        if (serviceProviderServiceId.equals(mfProviderServiceId))
            executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_make_supplier_refund", mergedData,request.getRequestId());
        else {
            //Get merchant Key
            String merchantKey = merchantPaymentProvider.get().getMerchantExternalKey();
            dataMap.put("merchantExternalKey", merchantKey);
            dataMap.put("merchantExternalId", merchantPaymentProvider.get().getMerchantExternalId());
            mergedData = mergeData(dataMap);
            executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_make_supplier_refund", mergedData,request.getRequestId());
            log.debug("executeWorkflowResponse--------------->{}", executeWorkflowResponse);
        }


        FinancialTransaction response = null;

        try {
            response = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, FinancialTransaction.class);
            log.debug("response1--------------->{}", response);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.debug("response2--------------->{}", response);

        response.setInvoice(transactionLog.get().getInvoice());
        response.setRefundId(response.getTransactionId());
        return response;
    }

    @Override
    public FinancialTransaction initiateRefundForApproval(RefundRequest request) {
        Optional<FinancialTransaction> transactionLog = financialRepository.findByExternalPaymentIdAndTransactionTypeAndInvoiceStatus(request.getKey(), "ExecutePaymentRequest", "Paid");
        if (transactionLog.isEmpty()) {
            transactionLog = financialRepository.findByPaymentIdAndTransactionTypeAndInvoiceStatus(request.getKey(), "ExecutePaymentRequest", "Paid");
            if (transactionLog.isEmpty()) {
                throw new InvoiceLinkExpiredOrNotFoundException();
            }
        }
        String alphaRefundId = "RF_"+UUID.randomUUID().toString();
        PendingRefundProcess pendingRefundProcess = new PendingRefundProcess();
        pendingRefundProcess.setAlphaRefundId(alphaRefundId);
        pendingRefundProcess.setKey(request.getKey());
        pendingRefundProcess.setKeyType(request.getKeyType());
        pendingRefundProcess.setComment(request.getComment());
        pendingRefundProcess.setSupplierDeductedAmount(request.getSupplierDeductedAmount());
        pendingRefundProcess.setRequestId(request.getRequestId());
        pendingRefundProcess.setMerchantId(request.getMerchantId());
        pendingRefundProcess.setMerchantName(request.getMerchantName());
        pendingRefundProcess.setStatus("PendingApproval");
        pendingRefundProcessRepository.save(pendingRefundProcess);
        String dummyWFResponse="{\n" +
                "  \"requestId\": \"REQ_ID\",\n" +
                "  \"status\": \"Success\",\n" +
                "  \"responseMessage\": \"Refund Created Successfully!\",\n" +
                "  \"paidCurrency\": \"AED\",\n" +
                "  \"processorId\": \"PROC_ID\",\n" +
                "  \"paidCurrencyValue\": \"SUPP_AMOUNT\",\n" +
                "  \"invoiceStatus\": \"Pending\",\n" +
                "  \"merchantId\": \"MERCH_ID\",\n" +
                "  \"paymentId\": \"PAY_ID\",\n" +
                "  \"currency\": \"AED\",\n" +
                "  \"comments\": \"COMMENT_\",\n" +
                "  \"transactionStatus\": \"Success\",\n" +
                "  \"transactionId\": \"REFUND_ID\",\n" +
                "  \"paymentResponse\": {\n" +
                "    \"key\": \"PAY_ID\",\n" +
                "    \"refundId\": \"REFUND_ID\",\n" +
                "    \"refundInvoiceId\": \"\",\n" +
                "    \"comment\": \"COMMENT_\"\n" +
                "  }\n" +
                "}";
        dummyWFResponse=dummyWFResponse.replaceAll("REQ_ID",request.getRequestId());
        dummyWFResponse=dummyWFResponse.replaceAll("PROC_ID",request.getProcessorId().toString());
        dummyWFResponse=dummyWFResponse.replaceAll("SUPP_AMOUNT",request.getSupplierDeductedAmount().toPlainString());
        dummyWFResponse=dummyWFResponse.replaceAll("MERCH_ID",request.getMerchantId().toString());
        dummyWFResponse=dummyWFResponse.replaceAll("PAY_ID",request.getKey());
        dummyWFResponse=dummyWFResponse.replaceAll("COMMENT_",request.getComment());
        dummyWFResponse=dummyWFResponse.replaceAll("REFUND_ID",alphaRefundId);
        log.debug("dummyWFResponse--------------->{}", dummyWFResponse);

        FinancialTransaction response = null;

        try {
            JsonNode rootNode = mapper.readTree(dummyWFResponse);

            response = objectMapper.treeToValue(rootNode, FinancialTransaction.class);
            log.debug("response1--------------->{}", response);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.debug("response2--------------->{}", response);
        response.setInvoice(transactionLog.get().getInvoice());
        response.setInvoiceLink(transactionLog.get().getInvoiceLink());
        response.setExternalInvoiceId(transactionLog.get().getExternalInvoiceId());
        response.setExternalPaymentId(transactionLog.get().getExternalPaymentId());
        response.setCustomerReference(transactionLog.get().getCustomerReference());
        //response.setRefundId(response.getTransactionId());
        response.setRefundId(response.getTransactionId());
        return response;

    }

    @Override
    public InitiateSessionResponse initiateSession(InitiateSessionRequest request) {
        Optional<PaymentLinkEntity> paymentLinkEntity = paymentLinkEntityRepository.findByInvoiceId(request.getInvoiceLink());
        assertPaymentLinkValid(paymentLinkEntity);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("requestId", request.getRequestId());
        JsonNode mergedData = mergeData(dataMap);

        Object executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_initiate_session", mergedData,request.getRequestId());
        InitiateSessionResponse response = null;
        try {
            response = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, InitiateSessionResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    @Override
    public VerifyResult processSignatureVerification(ValidateMBMERequest req) {

        Map<String, Object> mbme = new HashMap<>(req.getMbmeResponse()); // copy!
        String sigReceived = Objects.toString(mbme.remove("secure_sign"), null);
        if (sigReceived == null) {
            return new VerifyResult(false, "secure_sign missing");
        }

        String uid = Objects.toString(mbme.get("uid"), "");
        String extId = Objects.toString(mbme.get("mbme_payment_id"), "");

        MerchantPaymentProviderRegistration reg =
                merchantProviderRegistrationRepository
                        .findByMerchantExternalIdAndStatus(uid, "Active")
                        .stream().findFirst().orElse(null);

        if (reg == null) {
            return new VerifyResult(false, "No registration found");
        }

        String expected = SecureSignUtil.generateSecureSignForVerification(mbme,
                reg.getMerchantExternalKey(), "HmacMD5");

        boolean ok = sigReceived.equalsIgnoreCase(expected);
        log.debug("Signature verification: {}. Received: {}, Expected: {}",
                ok, sigReceived, expected);

        if (!ok) return new VerifyResult(false, "signature mismatch");

        // ---- business update ----
        financialRepository.findByExternalPaymentIdAndTransactionType(
                        extId, "ExecutePaymentRequest")
                .ifPresent(tran -> {
                    //tran.payment
                    tran.setTransactionStatus("Success");
                    tran.setResponseMessage("Payment Completed Successfully");
                    safeSave(tran);
                });

        return new VerifyResult(true, "signature valid");
    }


    /* --------------------------------------------------------------------- */
    /*  HANDLERS                                                             */
    /* --------------------------------------------------------------------- */

    @Override
    public void pushCredit(PaymentLinkEntity payLink, FinancialTransaction financialTransaction) {
        log.debug("pushCredit – invoiceId={}, invoiceRef={}",          // entry-point trace
                payLink.getInvoiceId(), payLink.getInvoiceReference());

        if (isAdditionalInputsEmpty(payLink)) {
            log.debug("pushCredit aborted – additionalInputs empty");
            return;
        }
        if (payLink.getAdditionalOutputs() != null && !payLink.getAdditionalOutputs().isEmpty()) {
            log.debug("pushCredit aborted – additionalOutputs not empty - processed before");
            return;
        }

        String invoiceType = getInvoiceType(payLink);
        if (invoiceType == null) {
            log.debug("pushCredit aborted – missing alphaInvoiceType");
            return;
        }

        switch (invoiceType) {
            case Constants.AGGREGAOR_SELFTOPUP -> handleSelfTopUp(payLink, financialTransaction);
            case Constants.AGGREGAOR_BILLPAYMENT -> handleBillPayment(payLink, financialTransaction);
            default -> log.warn("pushCredit skipped – unsupported invoiceType={}", invoiceType);
        }
    }

    void handleAutoRefundForFailedPushCredit(FinancialTransaction financialTransaction) {
        log.debug("handleAutoRefundForFailedPushCredit – invoiceId={}, invoiceRef={}",          // entry-point trace
                financialTransaction.getInvoiceLink(), financialTransaction.getExternalPaymentId());
        String refundId = UUID.randomUUID().toString();
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setKey(financialTransaction.getPaymentId());
        refundRequest.setKeyType("PaymentId");
        refundRequest.setComment("Refund for failed  credit at Aggregator Side for invoice=" + financialTransaction.getInvoiceLink());
        refundRequest.setSupplierDeductedAmount(financialTransaction.getAmount());
        refundRequest.setRequestId(refundId);
        FinancialTransaction refundStatus = processMakeRefundUpdated(refundRequest, financialTransaction.getProcessorId());

        FinancialTransaction refundTransaction = new FinancialTransaction();
        BeanUtility.copyProperties(refundRequest, refundTransaction);
        BeanUtility.copyProperties(refundStatus, refundTransaction);
        refundTransaction.setRequestId(refundId);
        refundTransaction.setTransactionType("MakeRefundRequest");
        //refundTransaction.setTransactionStatus(refundStatus.getTransactionStatus());
            financialTransactionLedgerService.save(refundTransaction);
    }

    private void handleSelfTopUp(PaymentLinkEntity payLink, FinancialTransaction financialTransaction) {
        ClientBalanceCreditInAggregatorOperationDTO req = buildSelfTopUpRequest(payLink);
        ResponseEntity<String> resp = invokePost(selfTopUpUrl, req, "selfTopUp");
        processCreditResponse(resp, ClientBalanceCreditInAggregatorOperationDTO.class, "selfTopUp", financialTransaction);
    }

    /* --------------------------------------------------------------------- */
    /*  BUILDERS                                                             */
    /* --------------------------------------------------------------------- */

    private void handleBillPayment(PaymentLinkEntity payLink, FinancialTransaction financialTransaction) {
        BillPaymentRequest req = buildBillPaymentRequest(payLink);
        ResponseEntity<String> resp = invokePost(paymentUrl, req, "billPayment");
        try {
            BillPaymentRequest response = objectMapper.readValue(resp.getBody(), BillPaymentRequest.class);
            if (response.getPaymentResponse() != null) {
                for (Map.Entry entry : response.getPaymentResponse().entrySet()) {
                    payLink.putAdditionalOutput(entry.getKey().toString(), entry.getValue());
                }
            }
        } catch (Throwable ex) {
            log.error("Error while parsing BillPaymentRequest response", ex);
        }

        processCreditResponse(resp, BillPaymentRequest.class, "billPayment", financialTransaction);
    }

    private ClientBalanceCreditInAggregatorOperationDTO buildSelfTopUpRequest(PaymentLinkEntity p) {
        log.debug("Building selfTopUp request for clientId={}", p.getAdditionalInputs().get("clientId"));
        ClientBalanceCreditInAggregatorOperationDTO dto = new ClientBalanceCreditInAggregatorOperationDTO();
        dto.setClientId(Long.valueOf(p.getAdditionalInputs().get("clientId") + ""));
        dto.setOperationAmount(p.getAmount());
        dto.setCurrency(p.getCurrency());
        dto.setRequestId(p.getInvoiceReference());
        return dto;
    }

    /* --------------------------------------------------------------------- */
    /*  LOW-LEVEL UTILITIES                                                  */
    /* --------------------------------------------------------------------- */

    private BillPaymentRequest buildBillPaymentRequest(PaymentLinkEntity p) {
        String[] parts = p.getInvoiceReference().split("#");
        String serviceId = parts[0];
        log.debug("Building billPayment request for serviceId={}", serviceId);

        BillPaymentRequest dto = new BillPaymentRequest();
        dto.setServiceId(serviceId);
        dto.setAmount(p.getAmount());
        dto.setCurrency(p.getCurrency());
        dto.setPaymentInputs(p.getAdditionalInputs());
        dto.setClientTransactionReference(p.getInvoiceReference());
        dto.setOperationType("PAYMENT");
        dto.setRequestId(p.getInvoiceReference());
        return dto;
    }

    private ResponseEntity<String> invokePost(String url, Object body, String ctx) {
        HttpEntity<Object> entity = new HttpEntity<>(body, buildHeaders());
        tracePayload(ctx + "-request", body);

        RestTemplate rt = restClientService.getGenericRestTemplate();
        rt.setErrorHandler(PASS_THRU_HANDLER);
        return restClientService.invokeRemoteService(url, HttpMethod.POST, entity, String.class, null, rt);
    }

    private void processCreditResponse(ResponseEntity<String> resp,
                                       Class<?> successClazz,
                                       String ctx, FinancialTransaction financialTransaction) {

        HttpStatusCode status = resp.getStatusCode();
        String rawBody = resp.getBody();
        log.debug("{} response – status={} body={}", ctx, status.value(), rawBody);

        try {
            if (status.is2xxSuccessful()) {
                Object ok = mapper.readValue(rawBody, successClazz);
                log.info("{} succeeded – {}", ctx, ok);
                /* TODO update status in DB */
                return;
            }
            ErrorResponse err = tryParseError(rawBody, status);
            log.error("{} failed (HTTP {}) – {} : {}",
                    ctx, status.value(), err.getErrorCode(), err.getErrorMessage());
            //TODO Check ERR CODE THEN PROCESS REFUND IN SPECIFIC CASES
            handleAutoRefundForFailedPushCredit(financialTransaction);

        } catch (Exception ex) {
            log.error("{} failed to process response", ctx, ex);
        }
    }

    private ErrorResponse tryParseError(String rawBody, HttpStatusCode status) {
        try {
            return mapper.readValue(rawBody, ErrorResponse.class);
        } catch (Exception ex) {
            ErrorResponse fallback = new ErrorResponse();
            fallback.setErrorCode(status.toString());
            fallback.setErrorMessage(rawBody);
            return fallback;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        return headers;
    }

    private boolean isAdditionalInputsEmpty(PaymentLinkEntity p) {
        return p.getAdditionalInputs() == null || p.getAdditionalInputs().isEmpty();
    }

    private String getInvoiceType(PaymentLinkEntity p) {
        Object type = p.getAdditionalInputs().get("alphaInvoiceType");
        return type == null ? null : type.toString();
    }

    private void tracePayload(String label, Object payload) {
        try {
            log.trace("{} => {}", label, mapper.writeValueAsString(payload));
        } catch (Exception ignored) {
        }
    }


    /*
    Added this metjhod for now as we noticed earlier methods hardcode mbme provider ID
     */
    @Override
    public FinancialTransaction processMakeRefundUpdated(RefundRequest request, Long providerId) {
        Optional<FinancialTransaction> transactionLog = financialRepository.findByPaymentIdAndTransactionType(request.getKey(), "ExecutePaymentRequest");
        if (transactionLog.isEmpty()) {
            throw new InvoiceLinkExpiredOrNotFoundException();
        }
        String serviceProviderServiceId = "NA";
        if (transactionLog.get().getProcessorId() != null) {
            if (transactionLog.get().getProcessorId() == 2) {
                serviceProviderServiceId = mbmeProviderServiceId;
            } else if (transactionLog.get().getProcessorId() == 1) {
                serviceProviderServiceId = mfProviderServiceId;
            }
        }
        Optional<MerchantPaymentProviderRegistration> merchantPaymentProvider =
                merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProviderRepository.findByServiceId(serviceProviderServiceId).get(), transactionLog.get().getMerchantId());

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("requestId", request.getRequestId());
        dataMap.put("merchantId", transactionLog.get().getMerchantId());
        dataMap.put("invoiceLink", transactionLog.get().getInvoiceLink());
        dataMap.put("paymentId", transactionLog.get().getPaymentId());
        dataMap.put("key", transactionLog.get().getExternalPaymentId());
        dataMap.put("keyType", request.getKeyType());
        dataMap.put("vendorDeductAmount", 0);
        dataMap.put("comment", request.getComment());
        dataMap.put("supplierCode", merchantPaymentProvider.get().getSupplierCode());
        dataMap.put("merchantExternalKey", merchantPaymentProvider.get().getMerchantExternalKey());
        dataMap.put("supplierDeductedAmount", request.getSupplierDeductedAmount());
        JsonNode mergedData = mergeData(dataMap);

        Object executeWorkflowResponse = null;
        if (serviceProviderServiceId.equals(mfProviderServiceId))
            executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_make_supplier_refund", mergedData,request.getRequestId());
        else
            executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_make_supplier_refund", mergedData,request.getRequestId());
        FinancialTransaction response = null;
        try {
            response = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, FinancialTransaction.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        BeanUtility.copyProperties(transactionLog.get(), response);
        response.setTransactionStatus("Pending");
        response.setTransactionType("MakeRefundRequest");

        return response;
    }


}