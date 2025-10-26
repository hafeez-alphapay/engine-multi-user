package com.alphapay.payEngine.alphaServices.dto.response;

import com.alphapay.payEngine.alphaServices.dto.request.InvoiceItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties
public class PaymentLinkResponse {
    private Date createdOn;
    private String invoiceId;
    private String paymentLinkTitle;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String customerName;
    private String language;
    private String customerContact;
    private String countryCode;
    private String customerEmail;
    private boolean isOpenAmount;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String comment;
    private boolean requiredTerms;
    private String termsCondition;
    private boolean customerKycRequired;
    private boolean signatureRequired;
    private String signatureUrl;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date expiry;
    private String paymentLinkUrl;
    private List<InvoiceItem> invoiceItems;
    private String invoiceStatus;
    private int totalPaymentAttempts;
    private int successfulAttempts;
    private String businessName;
}