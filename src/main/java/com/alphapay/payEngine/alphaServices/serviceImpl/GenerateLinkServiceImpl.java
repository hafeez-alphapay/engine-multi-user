package com.alphapay.payEngine.alphaServices.serviceImpl;

import com.alphapay.payEngine.account.management.dto.response.InvoiceStatusSummary;
import com.alphapay.payEngine.account.management.dto.response.InvoiceTypeSummaryResponse;
import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.account.management.exception.DateMismatchException;
import com.alphapay.payEngine.account.management.exception.MessageResolverService;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.service.BaseUserService;
import com.alphapay.payEngine.account.management.service.MerchantService;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.alphaServices.dto.request.*;
import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkCreationResponse;
import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkResponse;
import com.alphapay.payEngine.alphaServices.exception.TransactionAmountExceededException;
import com.alphapay.payEngine.alphaServices.exception.UnSupportedCurrencyException;
import com.alphapay.payEngine.alphaServices.model.InvoiceItemEntity;
import com.alphapay.payEngine.alphaServices.model.MerchantLinkSettings;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.model.StoreProductsEntity;
import com.alphapay.payEngine.alphaServices.repository.MerchantLinkSettingsRepository;
import com.alphapay.payEngine.alphaServices.repository.PaymentLinkEntityRepository;
import com.alphapay.payEngine.alphaServices.service.GenerateLinkService;
import com.alphapay.payEngine.common.bean.AuditInfo;
import com.alphapay.payEngine.integration.dto.request.GeneratePaymentGatewayInvoiceRequest;
import com.alphapay.payEngine.integration.exception.InvoiceLinkExpiredOrNotFoundException;
import com.alphapay.payEngine.integration.model.MerchantPaymentMethodsEntity;
import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;
import com.alphapay.payEngine.integration.model.PaymentMethodEntity;
import com.alphapay.payEngine.integration.repository.MerchantPaymentMethodsRepository;
import com.alphapay.payEngine.integration.repository.MerchantProviderRegistrationRepository;
import com.alphapay.payEngine.integration.service.InitiatePaymentService;
import com.alphapay.payEngine.notification.services.INotificationService;
import com.alphapay.payEngine.utilities.InvoiceStatus;
import com.alphapay.payEngine.utilities.UtilHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GenerateLinkServiceImpl implements GenerateLinkService {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private MessageResolverService msgResolver;

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private BaseUserService currentlyLoggedUser;

    @Autowired
    private UtilHelper utilHelper;

    @Autowired
    private PaymentLinkEntityRepository paymentLinkEntityRepository;

    @Autowired
    private InitiatePaymentService initiatePaymentService;

    @Value("${alphapay.merchant.base.link}")
    private String merchantLinkBaseUrl;

    @Value("${trans.history.page.size}")
    private String historyPageSize;

    @Value("${trans.history.duration}")
    private String transHistoryDuration;

    @Value("${default.unfiltered.max-results}")
    private String defaultUnfilteredMaxResults;

    @Value("${myfatoorah.call.back.url}")
    private String alphaPayCallBackUrl;

    @Value("${myfatoorah.call.error.url}")
    private String errorUrl;

    @Value("${myfatoorah.webhook.url}")
    private String alphaPayWebhookUrl;

    @Autowired
    private MerchantProviderRegistrationRepository merchantProviderRegistrationRepository;

    @Autowired
    private MerchantPaymentMethodsRepository merchantPaymentMethodsRepository;

    @Autowired
    private MerchantLinkSettingsRepository merchantLinkSettingsRepository;

    @Autowired
    private MerchantService merchantService;

    /**
     * @param request          --GenerateInvoiceRequest
     * @param callbackUrl      --callbackUrl
     * @param webhookUrl       --webhookUrl
     * @param webhookSecretKey --webhookSecretKey
     * @param serviceNameEn
     * @return --PaymentLinkCreationResponse
     */
    @Override
    public PaymentLinkCreationResponse generatePaymentGatewayInvoice(GenerateInvoiceRequest request, String callbackUrl, String webhookUrl, String webhookSecretKey, String serviceNameEn) {
        String prefix = "";
        return generateStandardLink(request, prefix, serviceNameEn, callbackUrl, webhookUrl, webhookSecretKey);
    }

    /**
     * @param request --GenerateInvoiceRequest
     * @return --PaymentLinkCreationResponse
     */
    @Override
    public PaymentLinkCreationResponse generateInvoiceLink(GenerateInvoiceRequest request) {
        String prefix = "";
        String type = "INVOICE";
        String webhookUrl = request.getWebhookUrl();
        String callbackUrl = request.getCallbackUrl();
        MerchantEntity merchantUser = merchantService.getMerchantByUserId(request.getAuditInfo().getUserId());
        request.setMerchantId(merchantUser.getId());
        return generateStandardLink(request, prefix, type, callbackUrl, webhookUrl, null, request.getAdditionalInputs());
    }


    @Override
    public PaymentLinkCreationResponse generateGenericPaymentLink(GenerateInvoiceRequest request, String prefix, String type, String callbackUrl, String webhookUrl, String webhookSecretKey) {
        MerchantEntity merchantUser = merchantService.getMerchantByUserId(request.getAuditInfo().getUserId());
        request.setMerchantId(merchantUser.getId());
        return generateStandardLink(request, prefix, type, callbackUrl, webhookUrl, webhookSecretKey);

    }

    public PaymentLinkCreationResponse generateStandardLink(GenerateInvoiceRequest request, String prefix, String type, String callbackUrl, String webhookUrl, String webhookSecretKey) {
        return generateStandardLink(request, prefix, type, callbackUrl, webhookUrl, webhookSecretKey, null);
    }

    public PaymentLinkCreationResponse generateStandardLink(GenerateInvoiceRequest request, String prefix, String type, String callbackUrl, String webhookUrl, String webhookSecretKey, Map<String, Object> additionalInputs) {

        MerchantLinkSettings merchantLinkSettings = merchantLinkSettingsRepository.findByMerchantIdAndCurrency(request.getAuditInfo().getUserId(), request.getCurrency());
        if (merchantLinkSettings != null) {
            if (request.getAmount().compareTo(merchantLinkSettings.getMaxTransactionAmount()) > 0) {
                throw new TransactionAmountExceededException(merchantLinkSettings.getMaxTransactionAmount(), merchantLinkSettings.getCurrency());
            }
        }

        if (!utilHelper.isSupportedCurrency(request.getCurrency())) {
            throw new UnSupportedCurrencyException();
        }

        PaymentLinkEntity link = new PaymentLinkEntity();

        BeanUtils.copyProperties(request, link);
        link.setOpenAmount(request.isOpenAmount());
        if (callbackUrl != null) {
            log.debug("callbackUrl--------->{}", callbackUrl);
            link.setCallBackUrl(callbackUrl);
        }
        if (webhookUrl != null)
            link.setWebhookUrl(webhookUrl);

        if (webhookSecretKey != null)
            link.setWebhookSecretKey(webhookSecretKey);

        if (request.getInvoiceItems() != null) {
            List<InvoiceItemEntity> items = new ArrayList<>();

            for (InvoiceItemRequest item : request.getInvoiceItems()) {
                InvoiceItemEntity invoiceItemEntity = new InvoiceItemEntity();
                invoiceItemEntity.setName(item.getName());
                invoiceItemEntity.setUnitPrice(item.getUnitPrice());
                invoiceItemEntity.setQuantity(item.getQuantity());
                items.add(invoiceItemEntity);
            }

            link.setInvoiceItems(items);
        }
        Long parentMerchantId = null;
        if (request instanceof GeneratePaymentGatewayInvoiceRequest) {
            parentMerchantId = ((GeneratePaymentGatewayInvoiceRequest) request).getParentId();
        }
        generatePayLink(prefix, link, type, 1, null, request.getExpiry(), request.getFixedDiscount(), request.getPercentageDiscount(), request.getAuditInfo(), request.getComment(), additionalInputs, parentMerchantId);
//        request.getAuditInfo.getUserId()
        PaymentLinkCreationResponse response = new PaymentLinkCreationResponse();
        BeanUtils.copyProperties(request, response);
        response.setInvoiceId(link.getInvoiceId());
        response.setPaymentLinkUrl(merchantLinkBaseUrl + link.getInvoiceId());
        final String applicationId = java.util.Objects.toString(httpServletRequest.getAttribute("applicationId"), "");
        if (request.isSendSms() && request.getCustomerContact() != null && !request.getCustomerContact().isEmpty()) {
            String[] msgKeys = {response.getPaymentLinkUrl()};
            notificationService.sendMobileNotification(request.getRequestId(), "CUSTOMER_PAYMENT_LINK_SMS", msgKeys, UtilHelper.getFullMobileNumber(request.getCustomerContact(), request.getCountryCode()), "", Locale.ENGLISH, applicationId, false);
        }

        if (request.isSendEmail() && request.getCustomerEmail() != null && !request.getCustomerEmail().isEmpty()) {
            String[] msgKeys = {response.getPaymentLinkUrl()};
            notificationService.sendEmailNotification(request.getRequestId(), "CUSTOMER_PAYMENT_LINK_EMAIL", msgKeys, request.getCustomerEmail(), "", Locale.ENGLISH, applicationId, null);
        }

        msgResolver.setAsSuccess(response);
        return response;
    }

    /**
     * Helper method to generate a payment link.
     *
     * @param payLinkEntity The payment link payLinkEntity to be populated.
     * @param type          The type of payment link.
     * @param maxRedemption The maximum redemption for the link.
     * @param product       The associated product payLinkEntity.
     * @param expiry        The expiry date of the link.
     * @param auditInfo
     * @param comment
     */
    @Transactional
    private void generatePayLink(String prefix, PaymentLinkEntity payLinkEntity, String type, long maxRedemption, StoreProductsEntity product, Date expiry, BigDecimal fixedDiscount, int percentageDiscount, AuditInfo auditInfo, String comment, Map<String, Object> additionalInputs, Long parentMerchantId) {
        MerchantEntity merchantUser = merchantService.getMerchantByUserId(auditInfo.getUserId());
        UserEntity userInfo = currentlyLoggedUser.getLoggedUser(auditInfo.getUserId());
        String nameOfUserCreateInvoice = "";

        if (parentMerchantId != null && parentMerchantId > 0) {
            // When a parent merchant ID is provided, fetch the parent MerchantEntity and
            // stamp the payment link with the parent owner's userId. This attributes the
            // created link to the parent merchant account (not the current sub-merchant),
            // ensuring correct ownership for reporting, permissions, and settlements.
            MerchantEntity parentMerchant = merchantService.getMerchant(parentMerchantId);
            payLinkEntity.setCreatedBy(parentMerchant.getOwnerUser().getId());
        } else {
            nameOfUserCreateInvoice = "-" + userInfo.getUserDetails().getFullName().stripLeading().split(" ")[0];
            payLinkEntity.setCreatedBy(auditInfo.getUserId());
        }
        Date currentDate = new Date();
        payLinkEntity.setCreatedOn(currentDate);

        payLinkEntity.setBusinessName(merchantUser.getLegalName() + nameOfUserCreateInvoice);
        payLinkEntity.setFixedDiscount(fixedDiscount);
        payLinkEntity.setPercentageDiscount(percentageDiscount);
        payLinkEntity.setExpiryDateTime(expiry);
//        payLinkEntity.setExpiryDateTime(utilHelper.getExpiryDate(expiry, currentDate));
        payLinkEntity.setMerchantUserAccount(merchantUser);
        payLinkEntity.setInvoiceStatus(InvoiceStatus.ACTIVE.getStatus());
        payLinkEntity.setSuccessfulAttempts(0);
        payLinkEntity.setTotalPaymentAttempts(0);
        payLinkEntity.setType(type);
        payLinkEntity.setQuantity(maxRedemption);
        payLinkEntity.setProduct(product);
        payLinkEntity.setComment(comment);
        if (additionalInputs != null && !additionalInputs.isEmpty()) {
            for (Map.Entry<String, Object> entry : additionalInputs.entrySet()) {
                payLinkEntity.putAdditionalInput(entry.getKey(), entry.getValue());
            }
        }

        try {
            payLinkEntity = paymentLinkEntityRepository.save(payLinkEntity);
            String paymentLink = buildPaymentLink(prefix, merchantUser.getId(), payLinkEntity.getId(), currentDate);
            if (paymentLink == null || paymentLink.isEmpty()) {
                log.error("Payment link generation failed for PayLinkEntity with ID: {}", payLinkEntity.getId());
                return;
            }
            payLinkEntity.setInvoiceId(paymentLink);
            for (InvoiceItemEntity item : payLinkEntity.getInvoiceItems()) {
                item.setPaymentLink(payLinkEntity); // Set FK relationship
            }
            paymentLinkEntityRepository.save(payLinkEntity);
            log.debug("generate payment link for PayLinkEntity: {}", payLinkEntity);
        } catch (Exception e) {
            log.error("Failed to generate payment link for PayLinkEntity: {}", payLinkEntity, e);
        }
    }

    /**
     * Builds a structured payment link.
     * Format: PAYLINK-{MerchantID}-{EntityID}-{Timestamp}
     *
     * @param merchantId The ID of the merchant user.
     * @param entityId   The ID of the saved PaymentLinkEntity.
     * @param createdOn  The creation date for timestamping.
     * @return A structured, unique payment link.
     */
    private String buildPaymentLink(String prefix, Long merchantId, Long entityId, Date createdOn) {
        long timestamp = createdOn.getTime();
//        return String.format("%s-%d-%d-%d", prefix, merchantId, entityId, timestamp);
        return String.format("%d-%d-%d", merchantId, entityId, timestamp);
    }

    /**
     *
     */
    @Override
    public List<InvoiceTypeSummaryResponse> getPaymentLinkStats(LinkHistoryRequest request) {
        // Validate date range
        if (request.getToDate() != null && request.getToDate().before(request.getFromDate())) {
            log.debug("Date mismatch: To date {} is before From date {}", request.getToDate(), request.getFromDate());
            throw new DateMismatchException();
        }

        if (request.getToDate() != null && request.getToDate().after(new Date())) {
            request.setToDate(new Date());
        }

        if (request.getFromDate() != null && request.getToDate() != null) {
            Duration duration = Duration.between(request.getFromDate().toInstant(), request.getToDate().toInstant());
            log.debug("Duration between dates: {} days", duration.toDays());
            Long daysBetween = duration.toDays();
            if (daysBetween > Long.parseLong(transHistoryDuration)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(request.getToDate());
                cal.add(Calendar.DATE, -Integer.parseInt(transHistoryDuration));
                request.setFromDate(cal.getTime());
            }
        }

        log.debug("Fetching payment links from date: {} to date: {}", request.getFromDate(), request.getToDate());
        Specification<PaymentLinkEntity> transactionSpec = new PayLinkSpecification(request);

        List<PaymentLinkEntity> entities;
        try {
            entities = paymentLinkEntityRepository.findAll(transactionSpec, Sort.by("createdOn").descending());
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching payment link data");
        }

        Map<String, Map<String, InvoiceTypeSummary>> groupedStats = new HashMap<>();

        for (PaymentLinkEntity entity : entities) {
            String type = entity.getType();
            String status = entity.getInvoiceStatus();

            groupedStats.computeIfAbsent(type, k -> new HashMap<>());
            groupedStats.get(type).computeIfAbsent(status, k -> new InvoiceTypeSummary());
            groupedStats.get(type).get(status).add(entity.getAmount());
        }

        List<InvoiceTypeSummaryResponse> responseList = groupedStats.entrySet().stream()
                .map(typeEntry -> {
                    String type = typeEntry.getKey();
                    Map<String, InvoiceTypeSummary> statusMap = typeEntry.getValue();

                    List<InvoiceStatusSummary> statusSummaries = statusMap.entrySet().stream()
                            .map(statusEntry -> {
                                String status = statusEntry.getKey();
                                InvoiceTypeSummary summary = statusEntry.getValue();
                                return new InvoiceStatusSummary(status, summary.getCount(), summary.getTotalAmount());
                            })
                            .collect(Collectors.toList());

                    return new InvoiceTypeSummaryResponse(type, statusSummaries);
                })
                .collect(Collectors.toList());
        return responseList;
    }

    @Override
    public PaginatedResponse<PaymentLinkResponse> getPaymentLinks(LinkHistoryRequest request) {
        // Validate date range
        if (request.getToDate() != null && request.getToDate().before(request.getFromDate())) {
//            log.debug("Date mismatch: To date {} is before From date {}", request.getToDate(), request.getFromDate());
            throw new DateMismatchException();
        }

        if (request.getToDate() != null && request.getToDate().after(new Date())) {
            request.setToDate(new Date());
        }

        if (request.getFromDate() != null && request.getToDate() != null) {
            Duration duration = Duration.between(request.getFromDate().toInstant(), request.getToDate().toInstant());
//            log.debug("Duration between dates: {} days", duration.toDays());
            Long daysBetween = duration.toDays();
            if (daysBetween > Long.parseLong(transHistoryDuration)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(request.getToDate());
                cal.add(Calendar.DATE, -Integer.parseInt(transHistoryDuration));
                request.setFromDate(cal.getTime());
            }
        }

//        log.debug("Fetching payment links from date: {} to date: {}", request.getFromDate(), request.getToDate());

        Specification<PaymentLinkEntity> transactionSpec = new PayLinkSpecification(request);
        Page<PaymentLinkEntity> transactionPage;
        try {
            int pageIndex = request.getPageNumber() == null ? 0 : request.getPageNumber() - 1;
            int pageSize = request.getPageSize() == null ? Integer.parseInt(historyPageSize) : request.getPageSize();
            PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("createdOn").descending());
            transactionPage = paymentLinkEntityRepository.findAll(transactionSpec, pageRequest);
        } catch (Exception ex) {
            log.error("Error fetching payment link data", ex);
            throw new RuntimeException("Error fetching payment link data");
        }

        List<PaymentLinkResponse> responses = transactionPage.getContent().stream().map(this::mapToResponse).toList();
        return new PaginatedResponse<>(responses,
                transactionPage.getNumber() + 1,
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast());
    }

    @Override
    public PaymentLinkResponse getLinkDetails(@Valid GetLinkDetails request) {
        Optional<PaymentLinkEntity> linkEntity = paymentLinkEntityRepository.findByInvoiceIdWithInvoiceItems(request.getInvoiceId());
        if (linkEntity.isEmpty()) throw new InvoiceLinkExpiredOrNotFoundException();
        return mapToResponse(linkEntity.get());
    }

    @Override
    @Transactional
    public PaymentLinkResponse markExpired(@Valid GetLinkDetails request) {
        Optional<PaymentLinkEntity> linkEntity = paymentLinkEntityRepository.findByInvoiceIdWithInvoiceItems(request.getInvoiceId());
        if (linkEntity.isEmpty()) throw new InvoiceLinkExpiredOrNotFoundException();
        linkEntity.get().setInvoiceStatus(InvoiceStatus.EXPIRED.getStatus());
        return mapToResponse(linkEntity.get());
    }

    /**
     * Helper method to map PaymentLinkEntity to PaymentLinkCreationResponse.
     */
    private PaymentLinkResponse mapToResponse(PaymentLinkEntity paymentLink) {
//        log.debug("mapToResponsePaymentLink{}", paymentLink);
        PaymentLinkResponse response = new PaymentLinkResponse();
        response.setCreatedOn(paymentLink.getCreatedOn());
        response.setPaymentLinkTitle(paymentLink.getPaymentLinkTitle());
        response.setDescription(paymentLink.getDescription());
        response.setAmount(paymentLink.getAmount());
        response.setCurrency(paymentLink.getCurrency());
        response.setCustomerName(paymentLink.getCustomerName());
        response.setCustomerContact(paymentLink.getCustomerContact());
        response.setCountryCode(paymentLink.getCountryCode());
        response.setMinAmount(paymentLink.getMinAmount());
        response.setMaxAmount(paymentLink.getMaxAmount());
        response.setComment(paymentLink.getComment());
        response.setRequiredTerms(paymentLink.isRequiredTerms());
        response.setOpenAmount(paymentLink.isOpenAmount());
        response.setTermsCondition(paymentLink.getTermsCondition());
        response.setExpiry(paymentLink.getExpiryDateTime());
        response.setPaymentLinkUrl(merchantLinkBaseUrl + paymentLink.getInvoiceId());
        response.setInvoiceId(paymentLink.getInvoiceId());
        response.setInvoiceStatus(paymentLink.getInvoiceStatus());
        response.setCustomerEmail(paymentLink.getCustomerEmail());
        response.setCustomerKycRequired(paymentLink.isCustomerKycRequired());
        response.setSignatureRequired(paymentLink.isSignatureRequired());
        response.setSignatureUrl(paymentLink.getSignatureUrl());
        response.setTotalPaymentAttempts(paymentLink.getTotalPaymentAttempts());
        response.setSuccessfulAttempts(paymentLink.getSuccessfulAttempts());
//        log.debug("InvoiceItems::::::{}", paymentLink.getInvoiceItems());
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        for (InvoiceItemEntity itemEntity : paymentLink.getInvoiceItems()) {
            InvoiceItem item = new InvoiceItem();
            BeanUtils.copyProperties(itemEntity, item);
            invoiceItems.add(item);
        }
//        log.debug("InvoiceItemsDTO::::::{}", invoiceItems);

        response.setInvoiceItems(invoiceItems);
        return response;
    }

    public class InvoiceTypeSummary {
        private long count = 0;
        private BigDecimal totalAmount = BigDecimal.ZERO;

        public void add(BigDecimal amount) {
            this.count++;
            this.totalAmount = this.totalAmount.add(amount != null ? amount : BigDecimal.ZERO);
        }

        // Getters
        public long getCount() {
            return count;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }

    /**
     * @param request
     * @return
     */
    @Override
    public List<PaymentMethodEntity> getPaymentMethod(GetMerchantPaymentMethod request) {
        Optional<MerchantPaymentProviderRegistration> defaultProviderOpt = merchantProviderRegistrationRepository.findFirstByMerchantIdAndIsDefaultTrue(request.getMerchantId());
        log.debug("defaultProviderOpt::{}", defaultProviderOpt);
        Long defaultProviderId = defaultProviderOpt.map(p -> p.getServiceProvider().getId()).orElse(-1L); // fallback to invalid ID
        log.debug("defaultProviderId::{}", defaultProviderId);
        String[] paymentMethodsCode = "ap,gp,uaecc".split(",");

        List<MerchantPaymentMethodsEntity> userPaymentMethods =
                merchantPaymentMethodsRepository.findByUserIdAndStatusOrderByDefaultProviderFirst(request.getMerchantId(), "Active", defaultProviderId, paymentMethodsCode);

        return userPaymentMethods.stream()
                .map(MerchantPaymentMethodsEntity::getPaymentMethod)
                .collect(Collectors.toList());
    }

}