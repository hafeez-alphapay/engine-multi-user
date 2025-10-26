package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.account.management.exception.MessageResolverService;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.account.merchantKyc.repository.MerchantRepository;
import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkCreationResponse;
import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.alphaServices.model.MerchantAlphaPayServicesEntity;
import com.alphapay.payEngine.alphaServices.model.MerchantServiceConfigEntity;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.repository.MerchantServicesRepository;
import com.alphapay.payEngine.alphaServices.repository.PaymentLinkEntityRepository;
import com.alphapay.payEngine.alphaServices.service.GenerateLinkService;
import com.alphapay.payEngine.alphaServices.service.MerchantAlphaPayServicesService;
import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentRequest;
import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentResponse;
import com.alphapay.payEngine.integration.dto.paymentData.InitiatePaymentRequest;
import com.alphapay.payEngine.integration.dto.paymentData.InitiatePaymentResponse;
import com.alphapay.payEngine.integration.dto.request.GeneratePaymentGatewayInvoiceRequest;
import com.alphapay.payEngine.integration.model.PaymentMethodEntity;
import com.alphapay.payEngine.integration.dto.request.PaymentStatusRequest;
import com.alphapay.payEngine.integration.exception.InvoiceLinkExpiredOrNotFoundException;
import com.alphapay.payEngine.integration.exception.MerchantIsNotAllowedForGW;
import com.alphapay.payEngine.integration.model.MerchantPaymentMethodsEntity;
import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;
import com.alphapay.payEngine.integration.repository.MerchantPaymentMethodsRepository;
import com.alphapay.payEngine.integration.repository.MerchantProviderRegistrationRepository;
import com.alphapay.payEngine.integration.service.ExecutePaymentService;
import com.alphapay.payEngine.integration.service.InitiatePaymentService;
import com.alphapay.payEngine.integration.service.PaymentGatewayService;
import com.alphapay.payEngine.utilities.BeanUtility;
import com.alphapay.payEngine.utilities.InvoiceStatus;
import com.alphapay.payEngine.utilities.UtilHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    @Value("${hash.config.Key}")
    private String configHashKey;

    @Value("${hash.config.salt}")
    private String configHashSalt;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private MessageResolverService resolverService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MerchantPaymentMethodsRepository merchantPaymentMethodsRepository;


    @Autowired
    private GenerateLinkService payLinkService;

    @Autowired
    private MerchantServicesRepository merchantServicesRepository;

    @Autowired
    private InitiatePaymentService initiatePaymentService;

    @Autowired
    private PaymentLinkEntityRepository paymentLinkEntityRepository;

    @Autowired
    private MerchantProviderRegistrationRepository merchantProviderRegistrationRepository;

    @Autowired
    private ExecutePaymentService executePaymentService;

    @Autowired
    private MerchantAlphaPayServicesService merchantAlphaPayServicesService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Override
    public PaymentLinkCreationResponse generatePaymentGateInvoice(GeneratePaymentGatewayInvoiceRequest request) {
        MerchantServiceConfigEntity merchantServiceConfigEntity = merchantAlphaPayServicesService.validatedMerchantApiKey(request.getApiKey());
        Long merchantId = merchantServiceConfigEntity.getMerchantId();
        request.setParentId(merchantServiceConfigEntity.getMerchantId());
        if (request.getMerchantId() != null && request.getMerchantId() != 0 && !request.getMerchantId().equals(merchantServiceConfigEntity.getMerchantId())) {
            //throw new InvoiceLinkExpiredOrNotFoundException();
            MerchantEntity merchantEntity = merchantRepository.findById(merchantServiceConfigEntity.getMerchantId()).orElseThrow(() -> new MerchantIsNotAllowedForGW());
            if (merchantEntity.getSubMerchants() == null || merchantEntity.getSubMerchants().isEmpty()) {
                throw new MerchantIsNotAllowedForGW();
            }
            Boolean allowed = false;
            for (MerchantEntity subMerchant : merchantEntity.getSubMerchants()) {
                if (subMerchant.getId().equals(request.getMerchantId())) {
                    allowed = true;
                    merchantId = subMerchant.getId();
                    break;
                }
            }
            if (!allowed) {
                throw new MerchantIsNotAllowedForGW();
            }
        }

        String callbackUrl = merchantServiceConfigEntity.getCallbackUrl();
        String webhookUrl = merchantServiceConfigEntity.getWebhookUrl();
        String webhookSecretKey = merchantServiceConfigEntity.getWebhookSecretKey();
        if (request.getCallbackUrl() != null && !request.getCallbackUrl().isEmpty()) {
            callbackUrl = request.getCallbackUrl();
        }
        if (request.getWebhookUrl() != null && !request.getWebhookUrl().isEmpty()) {
            webhookUrl = request.getWebhookUrl();
        }
//        request.getAuditInfo().setUserId(merchantId);
        request.setMerchantId(merchantId);
        //TODO::use cache to handle multiple DB call
        MerchantAlphaPayServicesEntity merchantAlphaPayServicesEntity = merchantAlphaPayServicesService.checkMerchantService(merchantId, request.getServiceId());
        MerchantAlphaPayServicesEntity merchantAlphaPayServices = merchantServicesRepository.findByMerchantIdAndAlphaPayServiceAndStatus(merchantId, merchantAlphaPayServicesEntity.getAlphaPayService(), "Active");
        PaymentLinkCreationResponse response = payLinkService.generatePaymentGatewayInvoice(request, callbackUrl, webhookUrl, webhookSecretKey, merchantAlphaPayServices.getAlphaPayService().getServiceNameEn());

        String requestId = request.getRequestId();
        Double amount = request.getAmount().doubleValue();
        String serviceId = request.getServiceId();
        String currency = request.getCurrency();
        String invoiceReference = request.getInvoiceReference();
        String description = request.getDescription();
        String customerName = request.getCustomerName();
        String paymentLinkUrl = response.getPaymentLinkUrl();

        response.setHash(UtilHelper.calculateCreateInvoiceHash(configHashKey, configHashSalt, requestId, amount, serviceId, currency, invoiceReference, description, customerName, paymentLinkUrl));
        return response;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public TransactionStatusResponse getTransactionStatus(PaymentStatusRequest request) {
        return getTransactionStatusByExternalIdOrPaymentId(request, Boolean.FALSE);
    }

    @Override
    public TransactionStatusResponse getTransactionStatusByExternalIdOrPaymentId(PaymentStatusRequest request) {
        log.trace("Payment  status request received: {}", request);
        merchantAlphaPayServicesService.validatedMerchantApiKey(request.getApiKey());
        log.trace("Merchant API Key validated successfully");
        return initiatePaymentService.processStatus(request,Boolean.TRUE);
    }

    @Override
    public TransactionStatusResponse getTransactionStatusByExternalIdOrPaymentId(PaymentStatusRequest request, Boolean bypassClientAPIKeyValidation) {
        if( !bypassClientAPIKeyValidation) {
            merchantAlphaPayServicesService.validatedMerchantApiKey(request.getApiKey());
        }
        return initiatePaymentService.processStatus(request);
    }

    /**
     * @param request
     * @return
     */
    @Override
    public InitiatePaymentResponse initiateDirectPayment(InitiatePaymentRequest request) {
        merchantAlphaPayServicesService.validatedMerchantApiKey(request.getApiKey());
        Optional<PaymentLinkEntity> paymentLinkEntity = paymentLinkEntityRepository.findByInvoiceId(request.getInvoiceId());
        if (paymentLinkEntity.isEmpty() || paymentLinkEntity.get().getInvoiceStatus().equals(InvoiceStatus.EXPIRED.getStatus())) {
            throw new InvoiceLinkExpiredOrNotFoundException();
        }

        PaymentLinkEntity paymentLink = paymentLinkEntity.get();
        assertPaymentLinkValid(paymentLinkEntity);

        String paymentId;

        if (paymentLink.getInvoiceStatus().equals(InvoiceStatus.ACTIVE.getStatus()) || paymentLink.getInvoiceStatus().equals(InvoiceStatus.PENDING.getStatus())) {
            paymentId = UUID.randomUUID().toString();
        } else {
            paymentId = "";
        }

        //update customer contact
        paymentLink.setCustomerEmail(request.getCustomerInfo().getCustomerEmail());
        paymentLink.setCustomerContact(request.getCustomerInfo().getCustomerContact());
        paymentLink.setCountryCode(request.getCustomerInfo().getCountryCode());
        paymentLink.setComment(request.getCustomerInfo().getCustomerComment());
        paymentLink.setPaymentId(paymentId);

        if (request.getCallbackUrl() != null)
            paymentLink.setCallBackUrl(request.getCallbackUrl());
        if (request.getWebhookUrl() != null)
            paymentLink.setWebhookUrl(request.getWebhookUrl());

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
        response.setPaymentId(paymentId);
        resolverService.setAsSuccess(response);
        log.debug("response::::{}", response);
        return response;
    }

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
     * @param request
     * @return
     */
    @Override
    public ExecutePaymentResponse executeDirectPayment(ExecutePaymentRequest request) {
        merchantAlphaPayServicesService.validatedMerchantApiKey(request.getApiKey());
        return executePaymentService.executePayment(request);
    }


    private void setPaymentMethods(InitiatePaymentResponse response, PaymentLinkEntity invoiceLog, Long defaultProviderId) {
        String[] paymentMethodsCode = "ap,gp,uaecc".split(",");
        if (invoiceLog.getPaymentMethodsCode() != null) {
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
    public void updateInvoiceWithPaymentId(InitiatePaymentResponse initiatePaymentResponse){
        Optional<PaymentLinkEntity> paymentLinkEntity =  paymentLinkEntityRepository.findByInvoiceId(initiatePaymentResponse.getInvoiceLink());
        if (paymentLinkEntity.isPresent()){
            paymentLinkEntity.get().setPaymentId(initiatePaymentResponse.getPaymentId());
        }
    }
}