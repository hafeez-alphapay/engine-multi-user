package com.alphapay.payEngine.integration.dto.response;

import com.alphapay.payEngine.alphaServices.model.InvoiceItemEntity;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@ToString
public class InvoiceSummaryResponse extends BaseResponse {
    private String type;
    private Date expiryDateTime;
    private String hash;
    private Long invoiceId;
    private String invoiceStatus;
    private PaymentLinkEntity invoice;
    private String invoiceLink;
    private Long merchantId;

    private String paymentId;

    private String externalPaymentId;

    private String description;

    private BigDecimal amount;

    private BigDecimal discountedPrice;

    private String paymentLinkTitle;

    private String currency;

    private boolean openAmount;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private String comment;

    private boolean requiredTerms;
    private boolean customerKycRequired;
    private String termsCondition;
    private CustomerInfo customerInfo;
    private List<InvoiceItemEntity> invoiceItems;
    private MerchantInvoiceInfoResponse merchantInfo;
}
