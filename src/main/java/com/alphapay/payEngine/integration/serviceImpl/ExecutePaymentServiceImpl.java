package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.account.management.exception.DuplicateEntryException;
import com.alphapay.payEngine.account.management.exception.MessageResolverService;
import com.alphapay.payEngine.account.management.service.MerchantService;
import com.alphapay.payEngine.alphaServices.exception.UnSupportedCurrencyException;
import com.alphapay.payEngine.alphaServices.model.InvoiceItemEntity;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.model.Redirect3DSUrl;
import com.alphapay.payEngine.alphaServices.repository.PaymentLinkEntityRepository;
import com.alphapay.payEngine.alphaServices.repository.Redirect3DSUrlRepository;
import com.alphapay.payEngine.alphaServices.serviceImpl.MerchantCountryPermissionCheckerImpl;
import com.alphapay.payEngine.config.MyFatoorahConfig;
import com.alphapay.payEngine.integration.dto.ProviderStatus;
import com.alphapay.payEngine.integration.dto.paymentData.*;
import com.alphapay.payEngine.integration.exception.*;
import com.alphapay.payEngine.integration.model.CurrencyExchangeRate;
import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;
import com.alphapay.payEngine.integration.model.PaymentMethodEntity;
import com.alphapay.payEngine.integration.repository.CurrencyExchangeRateRepository;
import com.alphapay.payEngine.integration.repository.InvoiceInitiatePaymentLogRepository;
import com.alphapay.payEngine.integration.repository.MerchantProviderRegistrationRepository;
import com.alphapay.payEngine.integration.repository.PaymentMethodRepository;
import com.alphapay.payEngine.integration.service.ExecutePaymentService;
import com.alphapay.payEngine.integration.service.MBMEIntegrationService;
import com.alphapay.payEngine.integration.service.ServiceProviderSwitcher;
import com.alphapay.payEngine.integration.service.WorkflowService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import com.alphapay.payEngine.utilities.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.alphapay.payEngine.utilities.UtilHelper.mergeData;

@Service
@Slf4j
public class ExecutePaymentServiceImpl implements ExecutePaymentService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${providers.mbme.id}")
    Long mbmeId;
    @Value("${providers.myFatoorah.id}")
    Long myFatoorahId;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MyFatoorahConfig config;
    @Autowired
    private MessageResolverService resolverService;
    @Value("${myfatoorah.api.baseUrl}")
    private String baseUrl;
    @Value("${myfatoorah.call.back.url}")
    private String callBackUrl;
    @Value("${myfatoorah.call.error.url}")
    private String errorUrl;
    @Value("${myfatoorah.webhook.url}")
    private String webhookUrl;
    @Value("${mbme.alphapay.hid}")
    private String hid;
    @Autowired
    private InvoiceInitiatePaymentLogRepository invoiceInitiatePaymentLogRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private FinancialTransactionRepository financialRepository;

    @Autowired
    private PaymentLinkEntityRepository paymentLinkEntityRepository;

    @Autowired
    private MerchantProviderRegistrationRepository merchantProviderRegistrationRepository;

    @Autowired
    private MBMEIntegrationService mbmeIntegrationService;

    @Autowired
    private WorkflowService workflowOrchestratorService;

    @Autowired
    private ServiceProviderSwitcher serviceProviderSwitcher;

    @Autowired
    private ProviderStatusService providerStatusService;

    @Value("${mbme.provider.service.id}")
    private String mbmeProviderServiceId;

    @Value("${default.tries.to.pay.invoice}")
    private String defaultTriesForInvoice;

    @Autowired
    private CurrencyExchangeRateRepository currencyExchangeRateRepository;

    @Value("${default.gateway.currency}")
    private String defaultGatewayCurrency;
    @Value("${alphapay.gateway.base.link}")
    private String alphaGatewayBaseLink;

    @Autowired
    private Redirect3DSUrlRepository redirect3DSUrlRepository;
    @Autowired
    private MerchantCountryPermissionCheckerImpl merchantCountryPermissionChecker;

    @Autowired
    private MerchantService merchantService;

    private static Card getCard(Card customerCard) {
        Card rawCard = new Card();
        rawCard.setCardHolderName(customerCard.getCardHolderName());
        rawCard.setNumber(customerCard.getNumber());
        rawCard.setExpiryYear(customerCard.getExpiryYear());
        rawCard.setExpiryMonth(customerCard.getExpiryMonth());
        rawCard.setSecurityCode(customerCard.getSecurityCode());
        return rawCard;
    }

    @Override
    public ExecutePaymentResponse executePayment(ExecutePaymentRequest request) {
        log.debug("executePayment request::{}", request);

        FinancialTransaction initiatePaymentLog = financialRepository.findFirstByPaymentIdAndTransactionTypeOrderByLastUpdatedDesc(request.getPaymentId(), PaymentStepsType.INITIATE_PAYMENT_S2.getName()).orElseThrow(CustomerInfoNotFoundException::new);

        Long merchantId = initiatePaymentLog.getMerchantId() != null ? initiatePaymentLog.getMerchantId() : request.getAuditInfo().getUserId();
        if (request.getCard() != null && request.getCard().getNumber() != null) {
            if (request.getCard().getNumber().length() > 6) {
                String bin = request.getCard().getNumber().substring(0, 6);
                boolean check = merchantCountryPermissionChecker.validateBINCodeForMerchant(bin, merchantId);
                if (check != true) {
                    throw new BINNotAllowedToMerchantException("BIN " + bin + " is not allowed for this merchant");
                }
            }
        }
        Map<String, String> customerInitiatePaymentInfo = initiatePaymentLog.getIncomingPaymentAttributes();
        BigDecimal exchangeRate = new BigDecimal(1);
        BigDecimal payableAmount = new BigDecimal(0);
        PaymentLinkEntity paymentLink = paymentLinkEntityRepository.findByPaymentIdWithInvoiceItems(request.getPaymentId())
                .orElseThrow(InvoiceLinkExpiredOrNotFoundException::new);

        BigDecimal invoiceValue = paymentLink.getAmount();

        if (paymentLink.getType().equals(PaymentProductType.PAYMENT_LINK.getName()) && paymentLink.isOpenAmount() ||
                paymentLink.getType().equals(PaymentProductType.STATIC_QR_LINK.getName())) {
            invoiceValue = initiatePaymentLog.getAmount();
        }

        // get currency rate based of display currency and convert amount based on rate.
        Optional<CurrencyExchangeRate> currencyExchangeRate = currencyExchangeRateRepository.findByCurrencyAndStatus(paymentLink.getCurrency(), "Active");
        if (currencyExchangeRate.isPresent()) {
            exchangeRate = currencyExchangeRate.get().getRate();
            payableAmount = invoiceValue.divide(exchangeRate, 2, RoundingMode.HALF_UP);
        } else {
            throw new UnSupportedCurrencyException();
        }

        //check  total payment attempts for not payment link and dynamic link
        if (paymentLink.getTotalPaymentAttempts() >= Integer.valueOf(defaultTriesForInvoice) && paymentLink.getType().equals(PaymentProductType.INVOICE_LINK.getName())) {
            paymentLink.setInvoiceStatus(InvoiceStatus.FAILED.getStatus());
            paymentLinkEntityRepository.save(paymentLink);
            throw new InvoiceLinkExpiredOrNotFoundException();
        } else {
            //update paymentLink details
            paymentLink.setCustomerName(customerInitiatePaymentInfo.get("customerName"));
            paymentLink.setCustomerContact(customerInitiatePaymentInfo.get("customerContact"));
            paymentLink.setCountryCode(customerInitiatePaymentInfo.get("countryCode"));
            paymentLink.setComment(customerInitiatePaymentInfo.get("customerComment"));
            paymentLink.setCustomerEmail(customerInitiatePaymentInfo.get("customerEmail"));
            paymentLink.setTotalPaymentAttempts(paymentLink.getTotalPaymentAttempts() + 1);
            paymentLinkEntityRepository.save(paymentLink);
        }


        List<MerchantPaymentProviderRegistration> merchantPaymentProviders = merchantProviderRegistrationRepository.findByMerchantIdAndStatus(paymentLink.getMerchantUserAccount().getId(), "Active");
        List<MerchantPaymentProviderRegistration> preferedProviders = merchantPaymentProviders.stream()
                .filter(MerchantPaymentProviderRegistration::getIsDefault)  // keep only those where isDefault == true
                .collect(Collectors.toList());

        log.debug("Defualts length {} , first{}", preferedProviders.size(), preferedProviders != null && preferedProviders.size() > 0 ? preferedProviders.get(0) : 0);

        if (merchantPaymentProviders == null || merchantPaymentProviders.size() == 0)
            throw new SupplierNotAssignedException();

        MerchantPaymentProviderRegistration defaultMerchantProvider = serviceProviderSwitcher.determineBestProvider(merchantPaymentProviders, preferedProviders);
        log.debug("selected default provider {}", defaultMerchantProvider);
        ProviderStatus providerStats = providerStatusService.getProviderStatus(defaultMerchantProvider.getServiceProvider().getId());



        /*
        for (MerchantPaymentProviderRegistration merchantPaymentProvider : merchantPaymentProviders) {
            if (!merchantPaymentProvider.getStatus().equals("Active")) {
                throw new SupplierNotAssignedException();
            }
            if (merchantPaymentProvider.getIsDefault()) {
                defaultMerchantProvider = merchantPaymentProvider;
            }
        }*/


        if (request.getSessionId() == null) {

          /*  ExecutePaymentRequest cardPayRequest = new ExecutePaymentRequest();
            cardPayRequest.setLanguage(language);
            cardPayRequest.setPaymentMethodId(request.getPaymentMethodId());
            cardPayRequest.setCustomerName(customerInfo.getCustomerName());
            cardPayRequest.setMobileCountryCode("+971");
            cardPayRequest.setCustomerMobile(UtilHelper.extractLocalNumber(customerInfo.getCustomerContact()));
            cardPayRequest.setCustomerEmail(customerInfo.getCustomerEmail());
            cardPayRequest.setCustomerReference(customerInfo.getCustomerContact());
            cardPayRequest.setCustomerAddress(prepareCustomerAddress(customerInfo));
            cardPayRequest.setInvoiceItems(prepareInvoiceItem(paymentLink));
            cardPayRequest.setInvoiceValue(paymentLink.getAmount().doubleValue());
            cardPayRequest.setDisplayCurrencyIso(paymentLink.getCurrency());
            cardPayRequest.setSuppliers(prepareSupplierList(defaultMerchantProvider, paymentLink.getAmount()));
            cardPayRequest.setExpiryDate(paymentLink.getExpiryDateTime().toString());

            cardPayRequest.setCallBackUrl(callBackUrl);
            cardPayRequest.setErrorUrl(callBackUrl);
            cardPayRequest.setWebhookUrl(webhookUrl);
            cardPayRequest.setRequestId(request.getRequestId());
*/
//            paymentLink.setCreatedBy("100");
            if (paymentLink.getAmount() != null) {
                paymentLink.setAmountString((paymentLink.getAmount()).toString().replaceAll("(\\.\\d*?)0+$", "$1")  // trim trailing zeros
                        .replaceAll("\\.$", ""));
            }
            log.debug(">>>>>>>>>>>>> merchant provider {} ,provider uid {} , provider key{}", defaultMerchantProvider.getId(), defaultMerchantProvider.getMerchantExternalId(), defaultMerchantProvider.getMerchantExternalKey());
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("executePayment", request);
            dataMap.put("supplierList", prepareSupplierList(defaultMerchantProvider, payableAmount));
            dataMap.put("customerInfo", customerInitiatePaymentInfo);
            dataMap.put("customerAddress", prepareCustomerAddress(customerInitiatePaymentInfo));
            dataMap.put("invoiceItems", prepareInvoiceItem(paymentLink, exchangeRate));
            dataMap.put("paymentLink", paymentLink);
            dataMap.put("callBackUrl", callBackUrl);//685/690
            dataMap.put("webhookUrl", webhookUrl);
            dataMap.put("merchantExternalId", defaultMerchantProvider.getMerchantExternalId());
            dataMap.put("merchantExternalKey", defaultMerchantProvider.getMerchantExternalKey());
            dataMap.put("invoiceValue", payableAmount);
            dataMap.put("displayCurrencyIso", defaultGatewayCurrency);
            JsonNode mergedData = mergeData(dataMap);
            Object executeWorkflowResponse = null;
            Long processorId = null;
            //TODO:: check default payment provider for merchant from merchant_payment_provider_registration based on it execute the workflow
            Optional<PaymentMethodEntity> paymentMethodEntity = paymentMethodRepository.findByPaymentMethodId(Integer.valueOf(request.getPaymentMethodId()));
            if (paymentMethodEntity.isEmpty()) {
                throw new PaymentMethodNotValidException();
            }
            //if (paymentMethodEntity.get().getServiceProvider().getServiceId().equals(mbmeProviderServiceId)) {
            if (defaultMerchantProvider.getServiceProvider().getId() == mbmeId) {
                executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mbme_direct_payment", mergedData, request.getRequestId());
                processorId = mbmeId;
            } else {
                executeWorkflowResponse = workflowOrchestratorService.executeWorkflow("mf_execute_payment", mergedData, request.getRequestId());
                processorId = myFatoorahId;

            }
            ExecutePaymentResponse response = null;
            try {
                response = objectMapper.treeToValue((JsonNode) executeWorkflowResponse, ExecutePaymentResponse.class);
            } catch (JsonProcessingException e) {

                if (providerStats != null) {
                    providerStats.getCircuitBreaker().recordFailure();
                    providerStats.getMetrics().recordFailure();
                }

                throw new RuntimeException(e);
            }
            log.debug("mf_execute_payment::{}", executeWorkflowResponse);
            response.setRequestId(request.getRequestId());
            if (providerStats != null) {
                providerStats.getCircuitBreaker().recordSuccess();
                providerStats.getMetrics().recordSuccess();

            }
            response.setProcessorId(processorId);
            response.setCardNumber(UtilHelper.maskCardNumber(request.getCard().getNumber()));
            response.setInvoice(paymentLink);
            //update paymentLink with paymentId to use it for get status when customer try to pay same invoice
            // and invoice status as pending
         /*   if (Objects.equals(defaultMerchantProvider.getServiceProvider().getId(), mbmeId)) {
                paymentLink.setExternalPaymentId(response.getExternalInvoiceId());
            } else {
                paymentLink.setExternalPaymentId(response.getResponseData().getPaymentId());
            }*/
//            paymentLink.setInvoiceStatus("Pending");
            paymentLinkEntityRepository.save(paymentLink);
            response.setExchangeRate(exchangeRate);
            response.setPaidCurrency(defaultGatewayCurrency);
            response.setPaidCurrencyValue(payableAmount);
            response.setAmount(paymentLink.getAmount());
            response.setCurrency(paymentLink.getCurrency());
            String externalPayment3DSUrl = response.getResponseData().getPaymentURL();
            String alpha3DSUUID = UUID.randomUUID().toString();
            Redirect3DSUrl redirect3DSUrl = new Redirect3DSUrl(alpha3DSUUID, response.getResponseData().getPaymentId(), response.getExternalPaymentId(), externalPayment3DSUrl);
            redirect3DSUrlRepository.save(redirect3DSUrl);
            response.getResponseData().setPaymentURL(alphaGatewayBaseLink + "redirect/3ds/" + defaultMerchantProvider.getServiceProvider().getId() + "/" + alpha3DSUUID);
            return response;


//            return callExecutePayment(cardPayRequest, customerCard, paymentLink);
        } else {
            ExecutePaymentRequest walletPayRequest = new ExecutePaymentRequest();
            walletPayRequest.setSessionId(request.getSessionId());
            walletPayRequest.setSuppliers(prepareSupplierList(defaultMerchantProvider, payableAmount));
            walletPayRequest.setCustomerName(customerInitiatePaymentInfo.get("customerName"));
            walletPayRequest.setDisplayCurrencyIso(defaultGatewayCurrency);
            walletPayRequest.setMobileCountryCode(customerInitiatePaymentInfo.get("countryCode"));
            walletPayRequest.setCustomerMobile(customerInitiatePaymentInfo.get("customerContact"));
            if (customerInitiatePaymentInfo.get("customerEmail") != null && !customerInitiatePaymentInfo.get("customerEmail").isEmpty()) {
                walletPayRequest.setCustomerEmail(customerInitiatePaymentInfo.get("customerEmail"));
            } else {
                walletPayRequest.setCustomerEmail("support@alphapay.ae");
            }
            walletPayRequest.setCallBackUrl(callBackUrl);
            walletPayRequest.setErrorUrl(callBackUrl);
            walletPayRequest.setWebhookUrl(webhookUrl);
            walletPayRequest.setLanguage("en");
            walletPayRequest.setInvoiceItems(prepareInvoiceItem(paymentLink, exchangeRate));
            walletPayRequest.setCustomerReference(customerInitiatePaymentInfo.get("customerContact"));
            walletPayRequest.setCustomerAddress(prepareCustomerAddress(customerInitiatePaymentInfo));
            walletPayRequest.setInvoiceValue(payableAmount);
            walletPayRequest.setRequestId(request.getRequestId());
            ExecutePaymentResponse response = callExecutePayment(walletPayRequest, null, paymentLink);
            response.setExchangeRate(exchangeRate);
            response.setPaidCurrency(defaultGatewayCurrency);
            response.setPaidCurrencyValue(payableAmount);
            return response;
        }
//        return (FinancialTransaction) mbmeIntegrationService.executeMbmePayment(request, customerCard, paymentLink);

    }

    private ExecutePaymentResponse callExecutePayment(ExecutePaymentRequest request, Card customerCard, PaymentLinkEntity paymentLink) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(config.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ExecutePaymentRequest> httpEntity = new HttpEntity<>(request, headers);
        CardInfoRequest cardInfoRequest = new CardInfoRequest();
        log.trace("ExecutePaymentRequest OUT-->{}", httpEntity);

        try {
            ResponseEntity<String> responseBody = restTemplate.exchange(baseUrl + "/v2/ExecutePayment", HttpMethod.POST, httpEntity, String.class);
            log.trace("ExecutePaymentResponse INN-->{}", responseBody.getBody());

            ExecutePaymentResponse response = ManualDeserializer.parseExecutePaymentResponse(responseBody.getBody());
            log.trace("ExecutePaymentResponse IN-->{}", response);

            if (response != null) {
                response.setExternalInvoiceId(response.getResponseData().getInvoiceId());
                FinancialTransaction transaction = financialRepository.findByRequestId(request.getRequestId());
                response.setResponseCode(200);
                response.setInvoiceId(paymentLink.getId());
                response.setInvoice(paymentLink);
                response.setAmount(paymentLink.getAmount());
                response.setCurrency(paymentLink.getCurrency());
                response.setMerchantId(paymentLink.getMerchantUserAccount().getId());
                response.setInvoiceLink(paymentLink.getInvoiceId());
                BeanUtility.copyProperties(response, transaction);
                transaction.setPaymentResponse(response.getResponseData().toMap());
                transaction.setRequestId(request.getRequestId());
                transaction.setCustomerReference(response.getResponseData().getCustomerReference());
                financialRepository.save(transaction);
                response.setPaymentURL(response.getResponseData().getPaymentURL());

                cardInfoRequest.setPaymentURL(response.getResponseData().getPaymentURL());
                cardInfoRequest.setPaymentType("card");
                if (customerCard != null) {
                    cardInfoRequest.setCard(getCard(customerCard));
                }
                return processDirectPayment(cardInfoRequest, response, paymentLink);
            } else {
                throw new CustomException("", "Failed During Execute Payment Please check with payment service provider");

            }

        } catch (HttpServerErrorException | HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.debug("responseBody:::>>>{}", responseBody);
            throw new CustomException("", responseBody);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntryException();
        } catch (CustomException e) {
            log.error("Error while executing paymentx: ", e);
            throw new CustomException("", e.getErrorMessage());
        } catch (Exception e) {
            log.error("Error while executing payment: ", e);
            throw new RuntimeException("Error while executing payment: " + e.getMessage(), e);
        }
    }

    private List<Supplier> prepareSupplierList(MerchantPaymentProviderRegistration merchantPaymentProvider, BigDecimal amount) {
        List<Supplier> suppliers = new ArrayList<>();
        Supplier supplier = new Supplier();
        supplier.setSupplierCode(merchantPaymentProvider.getSupplierCode());
        supplier.setInvoiceShare(amount);
        supplier.setProposedShare(null);
        suppliers.add(supplier);
        log.debug("supplier:::{}", supplier);
        return suppliers;
    }

    private CustomerAddressRequest prepareCustomerAddress(Map<String, String> customerInfo) {
        CustomerAddressRequest customerAddress = new CustomerAddressRequest();
        customerAddress.setAddress(customerInfo.get("address"));
        customerAddress.setAddressInstructions(customerInfo.get("addressInstructions"));
        customerAddress.setBlock(customerInfo.get("addressBlock"));
        customerAddress.setStreet(customerInfo.get("addressStreet"));
        customerAddress.setHouseBuildingNo(customerInfo.get("addressHouseBuildingNo"));
        return customerAddress;
    }

    private List<InvoiceItemRequest> prepareInvoiceItem(PaymentLinkEntity paymentLink, BigDecimal exchangeRate) {
        List<InvoiceItemRequest> invoiceItems = new ArrayList<>();
        for (InvoiceItemEntity items : paymentLink.getInvoiceItems()) {
            InvoiceItemRequest item = new InvoiceItemRequest();
            item.setItemName(items.getName());
            item.setQuantity(items.getQuantity());
            item.setUnitPrice(items.getUnitPrice().divide(exchangeRate, 2, RoundingMode.HALF_UP)); // convert amount based on exchangeRate
            invoiceItems.add(item);
        }
        return invoiceItems;
    }

    @Transactional
    private ExecutePaymentResponse processDirectPayment(CardInfoRequest cardInfoRequest, ExecutePaymentResponse executePaymentResponse, PaymentLinkEntity paymentLink) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());
        PaymentLinkEntity paymentLinkEntity = paymentLinkEntityRepository.findByInvoiceId(paymentLink.getInvoiceId()).orElseThrow(InvoiceLinkExpiredOrNotFoundException::new);


        HttpEntity<CardInfoRequest> requestEntity = new HttpEntity<>(cardInfoRequest, headers);
        try {
            log.trace("DirectPaymentRequest OUT-->{}", cardInfoRequest);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    cardInfoRequest.getPaymentURL(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            log.trace("DirectPaymentResponse IN-->{}", responseEntity);

            if (responseEntity.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)) {
                ExecutePaymentResponse response = objectMapper.readValue(responseEntity.getBody(), ExecutePaymentResponse.class);
                log.trace("DirectPaymentResponse2 IN-->{}", response);
                if (response != null) {
                    response.setExternalPaymentId(response.getResponseData().getPaymentId());
                    response.setTransactionStatus("InProgress");
                    executePaymentResponse.setTransactionStatus(response.getTransactionStatus());
                    executePaymentResponse.setExternalPaymentId(response.getExternalPaymentId());
                    executePaymentResponse.setPaymentURL(response.getResponseData().getPaymentURL());
                    executePaymentResponse.getResponseData().setPaymentId(response.getResponseData().getPaymentId());
                } else {
                    throw new CustomException("", "Failed During Execute Payment Please check with payment service provider");
                }
//                paymentLinkEntity.setExternalPaymentId(response.getResponseData().getPaymentId());
                paymentLinkEntity.setInvoiceStatus("Pending");
                response.setResponseMessage("Payment Initiated Successfully");
                BeanUtility.copyNonNullProperties(response, executePaymentResponse);
                executePaymentResponse.setProcessorId(1L);
                return executePaymentResponse;
            } else {
                log.error("Unexpected response content type: {}", responseEntity.getHeaders().getContentType());
                log.error("Raw response: {}", responseEntity.getBody());
//                paymentLinkEntity.setExternalPaymentId(executePaymentResponse.getResponseData().getPaymentId());
                paymentLinkEntity.setInvoiceStatus("Pending");
                executePaymentResponse.setPaymentHTML(responseEntity.getBody());
                executePaymentResponse.setTransactionStatus("InProgress");
                executePaymentResponse.setResponseMessage("Payment Initiated Successfully");
                executePaymentResponse.setProcessorId(1L);
                return executePaymentResponse;
            }
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("HttpServerErrorException::::{}", responseBody);
            throw new CustomException("Backend Service Processing Error ", responseBody);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntryException();
        } catch (HttpMessageNotReadableException e) {
            String responseBody = e.getMostSpecificCause().getMessage();
            log.error("Unexpected response: " + responseBody, e);
            throw new RuntimeException("Invalid response from payment service", e);
        } catch (RestClientException e) {
            String errorDetails = e.getMessage();
            throw new CustomException("Backend Service Processing Error ", errorDetails);
        } catch (Exception e) {
            throw new RuntimeException("Error while processing payment: " + e.getMessage());
        }
    }
}