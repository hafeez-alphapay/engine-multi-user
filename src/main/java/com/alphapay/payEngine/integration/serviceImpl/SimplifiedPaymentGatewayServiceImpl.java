package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkCreationResponse;
import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.service.MerchantAlphaPayServicesService;
import com.alphapay.payEngine.integration.dto.WebhookPushEvent;
import com.alphapay.payEngine.integration.dto.paymentData.*;
import com.alphapay.payEngine.integration.dto.request.*;
import com.alphapay.payEngine.integration.dto.response.RefundQueryResponse;
import com.alphapay.payEngine.integration.exception.AmountIsNotValidException;
import com.alphapay.payEngine.integration.exception.InvoiceLinkExpiredOrNotFoundException;
import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;
import com.alphapay.payEngine.integration.repository.MerchantProviderRegistrationRepository;
import com.alphapay.payEngine.integration.repository.ServiceProviderRepository;
import com.alphapay.payEngine.integration.service.InitiatePaymentService;
import com.alphapay.payEngine.integration.service.PaymentGatewayService;
import com.alphapay.payEngine.integration.service.SimplifiedPaymentGatewayService;
import com.alphapay.payEngine.financial.service.FinancialTransactionLedgerService;
import com.alphapay.payEngine.integration.service.WorkflowService;
import com.alphapay.payEngine.transactionLogging.DuplicateTransactionException;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import com.alphapay.payEngine.utilities.BeanUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.alphapay.payEngine.transactionLogging.PayEngineResponseLogger.SUCCESS_STATUS;
import static com.alphapay.payEngine.utilities.UtilHelper.mergeData;

/**
 * <p>
 * Default implementation of {@link SimplifiedPaymentGatewayService}.
 * The class provides convenience wrappers around {@link PaymentGatewayService}
 * that hide the multi‑step workflow of <i>invoice→ payment initiation→
 * logging</i>.
 * </p>
 *
 *
 * <p>
 * All public methods log both the entry parameters and the outcome at
 * <b>DEBUG</b> level to aid
 * troubleshooting in lower environments.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimplifiedPaymentGatewayServiceImpl implements SimplifiedPaymentGatewayService {

    /* Jackson instance reused for cheap POJO → Map projections */
    public static final String REFUNDED="Refunded";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final PaymentGatewayService paymentGatewayService;
    private final FinancialTransactionRepository financialRepo;
    @Autowired
    private InitiatePaymentService initiatePaymentService;


    @Autowired
    private ApplicationEventPublisher asyncEvent;
    @Autowired
    private MerchantAlphaPayServicesService merchantAlphaPayServicesService;
    @Autowired
    private FinancialTransactionRepository financialRepository;
    @Autowired
    private FinancialTransactionLedgerService financialTransactionLedgerService;
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;
    @Value("${mbme.provider.service.id}")
    private String mbmeProviderServiceId;

    @Value("${mf.provider.service.id}")
    private String mfProviderServiceId;

    @Value("${providers.ids.require.approvalForRefund}")
    private String requireApprovalProvidersIdsForRefund;
    @Autowired
    private MerchantProviderRegistrationRepository merchantProviderRegistrationRepository;
    @Autowired
    private WorkflowService workflowOrchestratorService;
    // ---------------------------------------------------------------------
    // Delegated one‑liner methods
    // ---------------------------------------------------------------------

    /**
     * Converts any POJO to a {@code Map<String,String>} by serialising it to a
     * Jackson <i>LinkedHashMap</i> first and then stringifying the values. Null
     * values are removed from the result.
     */
    public static Map<String, String> toStringMap(Object source) {
        if (source == null) {
            return Map.of();
        }

        // convert to Map<String,Object>
        Map<String, Object> asObjMap = MAPPER.convertValue(source, Map.class);

        // filter out nulls and stringify the values
        return asObjMap.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionStatusResponse getTransactionStatus(PaymentStatusRequest request) {
        log.debug("getTransactionStatus – requestId={}", request.getRequestId());
        return paymentGatewayService.getTransactionStatusByExternalIdOrPaymentId(request);
    }

    @Override
    public TransactionStatusResponse getTransactionStatus(PaymentStatusRequest request,
            Boolean bypassClientAPIKeyValidation) {
        log.debug("getTransactionStatus – requestId={}", request.getRequestId());
        return paymentGatewayService.getTransactionStatusByExternalIdOrPaymentId(request, bypassClientAPIKeyValidation);
    }

    // ---------------------------------------------------------------------
    // Compound workflow (invoice→ payment→ log)
    // ---------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutePaymentResponse executeDirectPayment(ExecutePaymentRequest request) {
        log.debug("executeDirectPayment – requestId={}", request.getRequestId());
        return paymentGatewayService.executeDirectPayment(request);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The workflow is broken into small, testable steps:
     * </p>
     * <ol>
     * <li>{@code generatePaymentInvoice}</li>
     * <li>{@code buildInitiatePaymentRequest}</li>
     * <li>{@code initiatePayment}</li>
     * <li>{@code persistTransaction}</li>
     * </ol>
     */
    @Override
    public InitiatePaymentResponse initiateDirectPayment(
            GeneratePaymentGatewayInvoiceAndInitiatePaymentRequest request) {

        log.debug("generateInvoiceCreationAndInitiateDirectPayment – requestId={} amount={}",
                request.getRequestId(), request.getAmount());

        // (1) Generate Invoice
        if (request.getCustomerInfo() != null) {
            if (request.getCustomerName() == null) {
                request.setCustomerName(request.getCustomerInfo().getCustomerName());
            }
            if (request.getCustomerEmail() == null) {
                request.setCustomerEmail(request.getCustomerInfo().getCustomerEmail());
            }
            if (request.getCustomerContact() == null) {
                request.setCustomerContact(request.getCustomerInfo().getCustomerContact());
            }
        }
        PaymentLinkCreationResponse invoice = generatePaymentInvoice(request);

        // Build Initiate Payment request
        InitiatePaymentRequest initiateReq = buildInitiatePaymentRequest(request, invoice);

        // (2) Initiate Payment
        InitiatePaymentResponse paymentResp = initiatePayment(initiateReq);
        paymentResp.setPaymentLinkUrl(invoice.getPaymentLinkUrl());
        // (3) update Invoice with PaymentId
        updateInvoiceWithPaymentId(paymentResp);
        // (4) Persist in Transaction Log (best‑effort)
        persistTransaction(paymentResp, initiateReq);

        return paymentResp;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public FinancialTransaction executeRefundDirectPayment(DirectPaymentRefundRequest request) {
        merchantAlphaPayServicesService.validatedMerchantApiKey(request.getApiKey());
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
        List<String> refundTypes = Arrays.asList("DirectPaymentRefundRequest", "PortalRefundRequest");

        List<FinancialTransaction> refundHistory = financialRepository
                .findAllByPaymentIdAndTransactionTypeInAndTransactionStatus(request.getPaymentId(),
                        refundTypes, "Success");
        BigDecimal totalRefundedAmount = refundHistory.stream()
                .map(FinancialTransaction::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
        FinancialTransaction refundStatus=null;
        refundRequest.setMerchantId(transactionLog.get().getMerchantId());
        refundRequest.setMerchantName(transactionLog.get().getInvoice().getBusinessName());
        refundRequest.setProcessorId(transactionLog.get().getProcessorId());
        if(providerIds.contains(transactionLog.get().getProcessorId()))
        {
            refundStatus = initiatePaymentService.initiateRefundForApproval(refundRequest);
        }
        else {

            refundStatus = initiatePaymentService.processRefund(refundRequest);
        }
            refundStatus.setRefundWebhookUrl(request.getWebhookUrl());
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

    /**
     * @param request
     * @return
     */
    @Override
    public RefundQueryResponse executeRefundStatus(RefundStatusRequest request) {
        TransactionStatusResponse webhookRequest = new TransactionStatusResponse();

        boolean notify=false;

        Optional<FinancialTransaction> transactionLog = financialRepository
                .findByPaymentIdAndTransactionTypeAndInvoiceStatus(request.getPaymentId(), "ExecutePaymentRequest",
                        "Paid");
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
        Optional<MerchantPaymentProviderRegistration> merchantPaymentProvider = merchantProviderRegistrationRepository
                .findByServiceProviderAndMerchantId(
                        serviceProviderRepository.findByServiceId(serviceProviderServiceId).get(),
                        transactionLog.get().getMerchantId());
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("requestId", request.getRequestId());
        dataMap.put("merchantId", transactionLog.get().getMerchantId());
        dataMap.put("invoiceLink", transactionLog.get().getInvoiceLink());
        dataMap.put("paymentId", transactionLog.get().getPaymentId());
        dataMap.put("key", transactionLog.get().getExternalInvoiceId());
        dataMap.put("keyType", "InvoiceId");
        dataMap.put("supplierCode", merchantPaymentProvider.get().getSupplierCode());
        PaymentLinkEntity paylink=transactionLog.get().getInvoice();

        JsonNode mergedData = mergeData(dataMap);

        Object executeWorkflowResponse = null;
        if (serviceProviderServiceId.equals(mfProviderServiceId)) {

            executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_check_refund_status",
                    mergedData,request.getRequestId());
        }
        else {
            // Get merchant Key
            String merchantKey = merchantPaymentProvider.get().getMerchantExternalKey();
            dataMap.put("merchantExternalKey", merchantKey);
            dataMap.put("merchantExternalId", merchantPaymentProvider.get().getMerchantExternalId());
            mergedData = mergeData(dataMap);
            executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_check_refund_status",
                    mergedData,request.getRequestId());
            log.debug("executeWorkflowResponseRefundStatus--------------->{}", executeWorkflowResponse);
        }

        merchantAlphaPayServicesService.validatedMerchantApiKey(request.getApiKey());
        //Add DirectPaymentRefundRequest or PortalRefundRequest
        List<String> refundTypes = Arrays.asList("DirectPaymentRefundRequest", "PortalRefundRequest");

        List<FinancialTransaction> refundHistroyLog = financialRepository
                .findAllByPaymentIdAndTransactionTypeIn(request.getPaymentId(), refundTypes);

        //List<FinancialTransaction> refundHistroyLog = financialRepository
        //.findAllByPaymentIdAndTransactionType(request.getPaymentId(), "DirectPaymentRefundRequest");
        RefundQueryResponse response = new RefundQueryResponse();

        ProviderRefundResponse providerResponse = null;
        try {
            providerResponse = MAPPER.treeToValue((JsonNode) executeWorkflowResponse, ProviderRefundResponse.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        BeanUtility.copyProperties(request, response);
        List<RefundQueryResponse.RefundQueryData> refundQueryDataList = new ArrayList<>();
        for (FinancialTransaction financialTransaction : refundHistroyLog) {
            RefundQueryResponse.RefundQueryData refundQueryData = new RefundQueryResponse.RefundQueryData();
            refundQueryData.setAmount(financialTransaction.getAmount());
            refundQueryData.setCurrency(financialTransaction.getCurrency());
            String txnStatus = financialTransaction.getTransactionStatus();
            refundQueryData.setTransactionStatus((txnStatus == null || txnStatus.isBlank()) ? "Failed" : txnStatus);
            refundQueryData.setRefundAmount(financialTransaction.getPaidCurrencyValue());
            refundQueryData.setRefundCurrency(financialTransaction.getPaidCurrency());
            refundQueryData.setComments(financialTransaction.getComments());
            String invStatus = financialTransaction.getInvoiceStatus();

            //refundQueryData.setInvoiceStatus((invStatus == null || invStatus.isBlank()) ? "Not Refunded" : invStatus);

            refundQueryData.setMerchantId(financialTransaction.getMerchantId());
            refundQueryData.setInvoiceLink(financialTransaction.getInvoiceLink());
            refundQueryData.setPaymentId(financialTransaction.getPaymentId());
            if(providerResponse!=null) {
                String refundId=financialTransaction.getExternalRefundId(); //Search By External Refund Id first
                if(refundId==null || refundId.isBlank())
                    refundId=financialTransaction.getRefundId(); //If Not there refund ID
                if(refundId==null || refundId.isBlank())
                {
                    refundId=financialTransaction.getTransactionId();
                }
                financialTransaction.setExternalRefundId(refundId);//Default it
                if(providerResponse.getResponseData()!=null && providerResponse.getResponseData().getRefunds_result()!=null && !providerResponse.getResponseData().getRefunds_result().isEmpty() )
                {
                    for(ProviderRefundResponse.RefundResult refundEntryResponse:providerResponse.getResponseData().getRefunds_result()) {
                        if(refundId!=null && refundId.equals(refundEntryResponse.getRefund_reference_id()))
                        {
                            refundQueryData.setRefundStatus(refundEntryResponse.getRefund_result());
                            refundQueryData.setRefundStatusMessage(refundEntryResponse.getRefund_status_message());
                            refundQueryData.setRefundId(financialTransaction.getRefundId().isBlank()?refundId: financialTransaction.getRefundId());
                            //TODO check this later
                            if(refundEntryResponse.getRefund_result()!=null && "unknown".equalsIgnoreCase(refundEntryResponse.getRefund_result()))
                            {
                                refundEntryResponse.setRefund_result("Cancelled");
                            }
                            if(refundEntryResponse.getRefund_result()!=null && !"Pending".equalsIgnoreCase(refundEntryResponse.getRefund_result()))

                            {
                                        webhookRequest.setRefundData(refundQueryData);
                                        webhookRequest.setRequestType("Update_Refund_Status");
                                        log.debug("New Status received  refund id {} -> {} >>>>> Pushing webhook",refundId,refundEntryResponse.getRefund_result());
                                        //webhookPusher.pushWebHook(webhookRequest,paylink,financialTransaction.getRefundWebhookUrl());
                                        asyncEvent.publishEvent(new WebhookPushEvent(webhookRequest,paylink,financialTransaction.getRefundWebhookUrl()));

                                    financialTransaction.setInvoiceStatus(refundEntryResponse.getRefund_result());
                            }
                            refundQueryDataList.add(refundQueryData);

                        }
                        else if ("Failed".equalsIgnoreCase(txnStatus))
                        {
                            refundQueryData.setRefundStatus("Canceled");
                            refundQueryData.setRefundStatusMessage(financialTransaction.getResponseMessage());
                            refundQueryDataList.add(refundQueryData);
                        }
                    }
                }
            }
        }
        response.setResponseData(refundQueryDataList);

        response.setStatus("Success");
        response.setResponseMessage("Refund Status Returned Successfully");
        return response;
    }

    // ---------------------------------------------------------------------
    // Step helpers
    // ---------------------------------------------------------------------

    private void updateInvoiceWithPaymentId(InitiatePaymentResponse paymentResp) {
        paymentGatewayService.updateInvoiceWithPaymentId(paymentResp);
    }

    private PaymentLinkCreationResponse generatePaymentInvoice(
            GeneratePaymentGatewayInvoiceAndInitiatePaymentRequest request) {
        log.debug("Generating payment invoice for requestId={} …", request.getRequestId());
        PaymentLinkCreationResponse response = paymentGatewayService.generatePaymentGateInvoice(request);
        log.debug("Invoice generated – requestUd={}", response.getRequestId());
        return response;
    }

    private InitiatePaymentRequest buildInitiatePaymentRequest(
            GeneratePaymentGatewayInvoiceAndInitiatePaymentRequest original,
            PaymentLinkCreationResponse invoice) {

        InitiatePaymentRequest initiateReq = new InitiatePaymentRequest();

        // copy needed fields from invoice & original request
        BeanUtility.copyProperties(invoice, initiateReq);
        BeanUtility.copyProperties(original, initiateReq);
        initiateReq.setCustomerInfo(original.getCustomerInfo());

        log.debug("InitiatePaymentRequest built – requestId={} amount={}",
                initiateReq.getRequestId(), initiateReq.getAmount());
        return initiateReq;
    }

    // ---------------------------------------------------------------------
    // Persistence helpers
    // ---------------------------------------------------------------------

    private InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) {
        log.debug("Initiating payment – requestId={} …", request.getRequestId());
        InitiatePaymentResponse response = paymentGatewayService.initiateDirectPayment(request);
        log.debug("Payment response received – status={} requestId={}",
                response.getStatus(), response.getRequestId());
        return response;
    }

    /**
     * Persists a unified view of the payment request & response. If the record
     * violates a unique constraint (duplicate transaction) a domain specific
     * {@link DuplicateTransactionException} is thrown to allow service callers
     * to take corrective action.
     */
    private void persistTransaction(InitiatePaymentResponse response, InitiatePaymentRequest request) {
        log.debug("Persisting financial transaction – requestId={} …", request.getRequestId());

        FinancialTransaction transaction = buildFinancialTransaction(response, request);

        try {
            financialTransactionLedgerService.save(transaction);
            log.debug("Transaction persisted – internalId={}", transaction.getId());
        } catch (Exception ex) {
            if (ex instanceof ConstraintViolationException || ex.getCause() instanceof ConstraintViolationException) {
                log.warn("Duplicate transaction detected – requestId={}", request.getRequestId());
                throw new DuplicateTransactionException();
            }
            log.error("Unexpected error while saving transaction", ex);
            throw ex;
        }
    }

    private FinancialTransaction buildFinancialTransaction(InitiatePaymentResponse response,
            InitiatePaymentRequest request) {
        FinancialTransaction tx = new FinancialTransaction();

        // Basic request fields
        BeanUtility.copyProperties(request, tx);
        tx.setTransactionType(request.getClass().getSimpleName());

        Optional.ofNullable(request.getAuditInfo()).ifPresent(audit -> BeanUtils.copyProperties(audit, tx));

        // Customer‑supplied dynamic attributes (flattened)
        tx.setIncomingPaymentAttributes(buildIncomingAttributesMap(request));

        // Default outcome fields – can be overridden by response copy below
        tx.setHttpResponseCode("200");
        tx.setResponseMessage(Optional.ofNullable(tx.getResponseMessage())
                .orElse("Transaction Completed Successfully"));
        tx.setAppResponseCode("0");
        tx.setStatus(SUCCESS_STATUS);

        // Overlay gateway response (status, ids, etc.)
        BeanUtility.copyProperties(response, tx);
        return tx;
    }

    // ---------------------------------------------------------------------
    // Utility – POJO → Map<String,String>
    // ---------------------------------------------------------------------

    private Map<String, String> buildIncomingAttributesMap(InitiatePaymentRequest request) {
        Map<String, String> attributes = new HashMap<>();
        Optional.ofNullable(request.getCustomerInfo())
                .ifPresent(info -> attributes.putAll(toStringMap(info)));
        Optional.ofNullable(request.getCustomerAddress())
                .ifPresent(addr -> attributes.putAll(toStringMap(addr)));
        return attributes;
    }

    @Override
    @Transactional
    public void executeRefundJob(String paymentId) {

        boolean notify=false;

        Optional<FinancialTransaction> transactionLog = financialRepository
                .findByPaymentIdAndTransactionTypeAndInvoiceStatus(paymentId, "ExecutePaymentRequest",
                        "Paid");
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
        Optional<MerchantPaymentProviderRegistration> merchantPaymentProvider = merchantProviderRegistrationRepository
                .findByServiceProviderAndMerchantId(
                        serviceProviderRepository.findByServiceId(serviceProviderServiceId).get(),
                        transactionLog.get().getMerchantId());
        Map<String, Object> dataMap = new HashMap<>();
        String requestId=UUID.randomUUID().toString();
        dataMap.put("requestId",requestId );
        dataMap.put("merchantId", transactionLog.get().getMerchantId());
        dataMap.put("invoiceLink", transactionLog.get().getInvoiceLink());
        dataMap.put("paymentId", transactionLog.get().getPaymentId());
        dataMap.put("key", transactionLog.get().getExternalPaymentId());
        dataMap.put("keyType", "PaymentId");
        dataMap.put("supplierCode", merchantPaymentProvider.get().getSupplierCode());
        PaymentLinkEntity paylink=transactionLog.get().getInvoice();



        JsonNode mergedData = mergeData(dataMap);

        Object executeWorkflowResponse = null;
        if (serviceProviderServiceId.equals(mfProviderServiceId))
            executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_make_supplier_refund",
                    mergedData,requestId);
        else {
            // Get merchant Key
            String merchantKey = merchantPaymentProvider.get().getMerchantExternalKey();
            dataMap.put("merchantExternalKey", merchantKey);
            dataMap.put("merchantExternalId", merchantPaymentProvider.get().getMerchantExternalId());
            mergedData = mergeData(dataMap);
            executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_check_refund_status",
                    mergedData,requestId);
            log.debug("executeWorkflowResponseRefundStatus--------------->{}", executeWorkflowResponse);
        }
        List<String> refundTypes = Arrays.asList("DirectPaymentRefundRequest", "PortalRefundRequest");
        List<FinancialTransaction> refundHistroyLog = financialRepository
                .findAllByPaymentIdAndTransactionTypeIn(paymentId, refundTypes);
        RefundQueryResponse response = new RefundQueryResponse();

        ProviderRefundResponse providerResponse = null;
        try {
            providerResponse = MAPPER.treeToValue((JsonNode) executeWorkflowResponse, ProviderRefundResponse.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<RefundQueryResponse.RefundQueryData> refundQueryDataList = new ArrayList<>();
        for (FinancialTransaction financialTransaction : refundHistroyLog) {
            RefundQueryResponse.RefundQueryData refundQueryData = new RefundQueryResponse.RefundQueryData();
            refundQueryData.setAmount(financialTransaction.getAmount());
            refundQueryData.setCurrency(financialTransaction.getCurrency());
            String txnStatus = financialTransaction.getTransactionStatus();
            refundQueryData.setTransactionStatus((txnStatus == null || txnStatus.isBlank()) ? "Failed" : txnStatus);
            refundQueryData.setRefundAmount(financialTransaction.getPaidCurrencyValue());
            refundQueryData.setRefundCurrency(financialTransaction.getPaidCurrency());
            refundQueryData.setComments(financialTransaction.getComments());
            String invStatus = financialTransaction.getInvoiceStatus();

            //refundQueryData.setInvoiceStatus((invStatus == null || invStatus.isBlank()) ? "Not Refunded" : invStatus);

            refundQueryData.setMerchantId(financialTransaction.getMerchantId());
            refundQueryData.setInvoiceLink(financialTransaction.getInvoiceLink());
            refundQueryData.setPaymentId(financialTransaction.getPaymentId());

            if(providerResponse!=null) {

                String refundId=financialTransaction.getExternalRefundId()!=null?financialTransaction.getExternalRefundId():financialTransaction.getRefundId(); //If Not there refund ID                if(refundId==null || refundId.isBlank())

               if(refundId==null || refundId.isBlank()) {
                    refundId=financialTransaction.getTransactionId();
                }
                if(providerResponse.getResponseData()!=null && providerResponse.getResponseData().getRefunds_result()!=null && !providerResponse.getResponseData().getRefunds_result().isEmpty() )
                {
                    for(ProviderRefundResponse.RefundResult refundEntryResponse:providerResponse.getResponseData().getRefunds_result()) {
                        if(refundId!=null && refundId.equals(refundEntryResponse.getRefund_reference_id()))
                        {
                            log.debug("<<<<<<<<< ProviderRefundResponse.RefundResult >>>>>>>>>{}",refundEntryResponse);
                            refundQueryData.setRefundStatus(refundEntryResponse.getRefund_result());
                            refundQueryData.setRefundStatusMessage(refundEntryResponse.getRefund_status_message());
                            refundQueryData.setRefundId(financialTransaction.getRefundId()!=null?financialTransaction.getRefundId():refundId);
                            //TODO check this later
                            if(refundEntryResponse.getRefund_result()!=null && "unknown".equalsIgnoreCase(refundEntryResponse.getRefund_result()))
                            {
                                refundEntryResponse.setRefund_result("Pending");
                            }
                            if(refundEntryResponse.getRefund_result()!=null && !"Pending".equalsIgnoreCase(refundEntryResponse.getRefund_result()))
                            {
                                TransactionStatusResponse webhookRequest = new TransactionStatusResponse();
                                webhookRequest.setRefundData(refundQueryData);
                                webhookRequest.setRequestType("Update_Refund_Status");
                                log.debug("New Status received  refund id {} -> {} >>>>> Pushing webhook",refundId,refundEntryResponse.getRefund_result());
                                asyncEvent.publishEvent(new WebhookPushEvent(webhookRequest,paylink,financialTransaction.getRefundWebhookUrl()));
                                financialTransaction.setInvoiceStatus(refundEntryResponse.getRefund_result());
                            }
                            refundQueryDataList.add(refundQueryData);

                        }
                        else if ("Failed".equalsIgnoreCase(txnStatus))
                        {
                            refundQueryData.setRefundStatus("Canceled");
                            refundQueryData.setRefundStatusMessage(financialTransaction.getResponseMessage());
                            refundQueryDataList.add(refundQueryData);
                        }
                    }
                }
            }
        }
        response.setResponseData(refundQueryDataList);

        response.setStatus("Success");
        response.setResponseMessage("Refund Status Returned Successfully");
    }

}