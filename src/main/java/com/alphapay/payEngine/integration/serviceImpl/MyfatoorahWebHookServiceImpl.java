package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.roles.model.RoleEntity;
import com.alphapay.payEngine.account.roles.model.UserRoleEntity;
import com.alphapay.payEngine.account.roles.repository.RoleRepository;
import com.alphapay.payEngine.account.roles.repository.UserRoleRepository;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.repository.PaymentLinkEntityRepository;
import com.alphapay.payEngine.integration.dto.request.MyFatoorahaWebhookRequest;
import com.alphapay.payEngine.integration.model.PaymentMethodEntity;
import com.alphapay.payEngine.integration.dto.response.AlphaWebhookResponse;
import com.alphapay.payEngine.integration.model.MerchantPaymentMethodsEntity;
import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;
import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import com.alphapay.payEngine.integration.repository.MerchantPaymentMethodsRepository;
import com.alphapay.payEngine.integration.repository.MerchantProviderRegistrationRepository;
import com.alphapay.payEngine.integration.repository.PaymentMethodRepository;
import com.alphapay.payEngine.integration.repository.ServiceProviderRepository;
import com.alphapay.payEngine.integration.service.MyfatoorahWebHookService;
import com.alphapay.payEngine.notification.services.INotificationService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import com.alphapay.payEngine.utilities.BeanUtility;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class MyfatoorahWebHookServiceImpl implements MyfatoorahWebHookService {

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private MerchantProviderRegistrationRepository merchantProviderRegistrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MerchantPaymentMethodsRepository merchantPaymentMethodsRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private PaymentLinkEntityRepository paymentLinkEntityRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${mf.provider.service.id}")
    private String mfProviderServiceId;

    @Override
    public void processWebHookResponse(MyFatoorahaWebhookRequest request) throws Exception {

        if (request.getEventType() == 1) {
//            handleTransactionStatusChanged(request);
        }
        if (request.getEventType() == 2) {
            handleRefundStatusChanged(request);
        }
        if (request.getEventType() == 3) {
            handleBalanceTransferred(request);
        }
        if (request.getEventType() == 4) {
            handleSupplierStatusChanged(request);
        }
    }


    private void handleSupplierStatusChanged(MyFatoorahaWebhookRequest request) {
        String supplierCode = request.getData().getSupplierCode().toString();
        String supplierName = request.getData().getSupplierName();
        String supplierMobile = request.getData().getSupplierMobile();
        String supplierEmail = request.getData().getSupplierEmail();
        String supplierStatus = request.getData().getSupplierStatus();
        String comments = "N/A";
        String rejectReasons = "N/A";
        if (request.getData().getKycFeedback() != null) {
            comments = request.getData().getKycFeedback().getComments();
            rejectReasons = request.getData().getKycFeedback().getRejectReasons();
        }
        String[] msgKeys = {supplierCode, supplierName, supplierMobile, supplierEmail, supplierStatus, comments, rejectReasons};
        notificationService.sendEmailNotification(UUID.randomUUID().toString(), "WEBHOOK_RESPONSE_SUPPLIER", msgKeys, "m.alshayib@alphapay.ae", "", Locale.ENGLISH, "004779", "email_webhook_notification.html");
        Optional<MerchantPaymentProviderRegistration> supplierDetailsEntity = merchantProviderRegistrationRepository.findBySupplierCode(request.getData().getSupplierCode());
        if (supplierDetailsEntity.isPresent()) {
            if (request.getData().getSupplierStatus().equalsIgnoreCase("APPROVED")) {
                supplierDetailsEntity.get().setStatus("Active");
            }
            if (request.getData().getKycFeedback() != null) {
                String kycFeedback = supplierDetailsEntity.get().getKycStatus();
                supplierDetailsEntity.get().setKycStatus(request.getData().getSupplierStatus());
                supplierDetailsEntity.get().setKycFeedback(kycFeedback);
            }
            merchantProviderRegistrationRepository.save(supplierDetailsEntity.get());
            Optional<UserEntity> user = userRepository.findById(supplierDetailsEntity.get().getMerchantId());
            //change merchant  Myfattora Approve Status
//            user.get().setMyfattoraApproveStatus(request.getData().getSupplierStatus());
            userRepository.save(user.get());
            //assign merchant role
            List<UserRoleEntity> userRoleEntity = userRoleRepository.findByUser(user.get());
            if (userRoleEntity.isEmpty()) {
                RoleEntity role = roleRepository.findByName("Merchant");
                UserRoleEntity newUserRole = new UserRoleEntity();
                newUserRole.setUser(user.get());
                newUserRole.setRoleEntity(role);
                newUserRole.setStatus("Active");
                userRoleRepository.save(newUserRole);
            }
            //set merchant payment method
            List<MerchantPaymentMethodsEntity> userPaymentMethodsEntities = merchantPaymentMethodsRepository.findByUserId(user.get().getId());
            if (userPaymentMethodsEntities.isEmpty()) {
                List<MerchantPaymentMethodsEntity> newUserPayMethod = new ArrayList<>();
                Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findByServiceId(mfProviderServiceId);
                List<PaymentMethodEntity> payMethodList = paymentMethodRepository.findByStatusAndServiceProvider("Active", serviceProvider.get());
                for (PaymentMethodEntity payMethod : payMethodList) {
                    MerchantPaymentMethodsEntity userPaymentMethods = new MerchantPaymentMethodsEntity();
                    userPaymentMethods.setPaymentMethod(payMethod);
                    userPaymentMethods.setUserId(user.get().getId());
                    userPaymentMethods.setStatus("Active");
                    newUserPayMethod.add(userPaymentMethods);
                }
                merchantPaymentMethodsRepository.saveAll(newUserPayMethod);
            }
        }
    }

    private void handleBalanceTransferred(MyFatoorahaWebhookRequest request) {
        String[] msgKeys = {request.toString()};
        notificationService.sendEmailNotification(UUID.randomUUID().toString(), "MERCHANT_ONBOARDING_REGISTRATION_OTP_EMAIL", msgKeys, "m.alshayib@alphapay.ae", "", Locale.ENGLISH, "004779", null);

    }

    @Transactional
    private void handleRefundStatusChanged(MyFatoorahaWebhookRequest request) {
        String[] msgKeys = {request.toString()};
        notificationService.sendEmailNotification(UUID.randomUUID().toString(), "MERCHANT_ONBOARDING_REGISTRATION_OTP_EMAIL", msgKeys, "m.alshayib@alphapay.ae", "", Locale.ENGLISH, "004779", null);
        Optional<FinancialTransaction> financialTransaction = financialTransactionRepository.findByExternalPaymentIdAndTransactionType(request.getData().getGatewayReference().getPaymentId(), "MakeRefundRequest");
        financialTransaction.get().setTransactionStatus(request.getData().getRefundStatus());
        financialTransaction.get().setPaymentMethod(request.getData().getGatewayReference().getPaymentMethod());
        financialTransaction.get().setPaidCurrencyValue(request.getData().getGatewayReference().getRefundAmount().negate());
        financialTransaction.get().setPaidCurrency(request.getData().getGatewayReference().getCurrency());
        financialTransaction.get().setTransactionId(request.getData().getGatewayReference().getTransactionId());
        financialTransaction.get().setComments(request.getData().getComments());
    }

    @Transactional
    private void handleTransactionStatusChanged(MyFatoorahaWebhookRequest request) throws Exception {
        Optional<FinancialTransaction> financialTransaction = financialTransactionRepository.findByExternalPaymentIdAndTransactionType(request.getData().getPaymentId(), "ExecutePaymentRequest");
        if (financialTransaction.isPresent()) {
            financialTransaction.get().setTransactionStatus(request.getData().getTransactionStatus());
            financialTransaction.get().setPaymentMethod(request.getData().getPaymentMethod());
            financialTransaction.get().setPaidCurrencyValue(request.getData().getInvoiceValueInPayCurrency());
            financialTransaction.get().setPaidCurrency(request.getData().getPayCurrency());
        }
        Long InvoiceId = request.getData().getInvoiceId();
        String InvoiceReference = request.getData().getInvoiceReference();
        String CreatedDate = request.getData().getCreatedDate();
        String CustomerReference = request.getData().getCustomerReference();
        String CustomerName = request.getData().getCustomerName();
        String CustomerMobile = request.getData().getCustomerMobile();
        String CustomerEmail = request.getData().getCustomerEmail();
        String TransactionStatus = request.getData().getTransactionStatus();
        String PaymentMethod = request.getData().getPaymentMethod();
        String ReferenceId = request.getData().getReferenceId();
        String TrackId = request.getData().getTrackId();
        String PaymentId = request.getData().getPaymentId();
        String AuthorizationId = request.getData().getAuthorizationId();
        String InvoiceValueInBaseCurrency = request.getData().getInvoiceValueInBaseCurrency();
        String BaseCurrency = request.getData().getBaseCurrency();
        String InvoiceValueInDisplayCurreny = request.getData().getInvoiceValueInDisplayCurrency();
        String DisplayCurrency = request.getData().getDisplayCurrency();
        BigDecimal InvoiceValueInPayCurrency = request.getData().getInvoiceValueInPayCurrency();
        String PayCurrency = request.getData().getPayCurrency();

        String[] msgKeys = {String.valueOf(InvoiceId), InvoiceReference, CreatedDate, CustomerReference, CustomerName, CustomerMobile, CustomerEmail, TransactionStatus, PaymentMethod, ReferenceId, TrackId, PaymentId, AuthorizationId, InvoiceValueInBaseCurrency, BaseCurrency, InvoiceValueInDisplayCurreny, DisplayCurrency, String.valueOf(InvoiceValueInPayCurrency), PayCurrency};

        notificationService.sendEmailNotification(UUID.randomUUID().toString(), "WEBHOOK_RESPONSE_TRANSACTION", msgKeys, "m.alshayib@alphapay.ae", "", Locale.ENGLISH, "004779", "email_webhook_notification.html");

        AlphaWebhookResponse response = new AlphaWebhookResponse();
        BeanUtility.copyProperties(request, response);
        BeanUtility.copyProperties(request.getData(), response);
        BeanUtility.copyProperties(request.getData().getGatewayReference(), response);

        Optional<PaymentLinkEntity> paymentLinkEntity = paymentLinkEntityRepository.findByExternalPaymentId(response.getPaymentId());
      /*  if (!paymentLinkEntity.get().getWebhookUrl().isEmpty()) {
            String generatedSignature = generateSignature(response, paymentLinkEntity.get().getWebhookSecretKey());
            response.setInvoiceId(paymentLinkEntity.get().getInvoiceId());
            response.setInvoiceReference(paymentLinkEntity.get().getInvoiceReference());
            HttpHeaders headers = new HttpHeaders();

            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Signature", generatedSignature);
            HttpEntity<AlphaWebhookResponse> httpEntity = new HttpEntity<>(response, headers);

            try {
                restTemplate.exchange(paymentLinkEntity.get().getWebhookUrl(), HttpMethod.POST, httpEntity, void.class);
            } catch (RestClientException e) {
                throw new RuntimeException(e);
            }
        }*/
    }
}
