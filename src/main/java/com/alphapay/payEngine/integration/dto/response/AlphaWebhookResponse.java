package com.alphapay.payEngine.integration.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class AlphaWebhookResponse {
    private int eventType;
    private String event;
    private String invoiceId;
    private String invoiceReference;
    private String createdDate;
    private String customerReference;
    private String customerName;
    private String customerMobile;
    private String customerEmail;
    private String transactionStatus;
    private String paymentMethod;
    private String referenceId;
    private String trackId;
    private String paymentId;
    private String authorizationId;
    private BigDecimal invoiceValueInBaseCurrency;
    private String baseCurrency;
    private BigDecimal invoiceValueInDisplayCurrency;
    private String displayCurrency;
    private BigDecimal invoiceValueInPayCurrency;
    private String payCurrency;
}
