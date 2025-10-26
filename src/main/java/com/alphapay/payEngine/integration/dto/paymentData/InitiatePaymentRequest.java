package com.alphapay.payEngine.integration.dto.paymentData;

import com.alphapay.payEngine.integration.dto.request.CustomerAddress;
import com.alphapay.payEngine.integration.dto.response.CustomerInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiatePaymentRequest extends BaseFinancialRequest {
    private BigDecimal amount;
    private String paymentId;

    private String apiKey;

    private String invoiceId;

    private String callbackUrl;

    private String webhookUrl;

    private CustomerInfo customerInfo;

    private CustomerAddress customerAddress;
}
