package com.alphapay.payEngine.alphaServices.dto.response;

import com.alphapay.payEngine.model.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentLinkCreationResponse extends BaseResponse {
    private String invoiceId;
    private String paymentLinkTitle;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String customerName;
    private String language;
    private String customerContact;
    private String countryCode;
    private boolean isOpenAmount;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String comment;
    private boolean requiredTerms;
    private String termsCondition;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date expiry;
    private String paymentLinkUrl;
    private String invoiceStatus;
    private String hash;
}