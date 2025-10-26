package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.integration.dto.paymentData.BaseFinancialRequest;
import com.alphapay.payEngine.integration.dto.response.CustomerInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InitiateDirectPaymentRequest extends BaseFinancialRequest {
    @NotBlank(message = "apiKey is required")
    private String apiKey;

    @NotBlank(message = "Invoice Id is required")
    private String invoiceId;

    private String callbackUrl;

    private String webhookUrl;

    private CustomerInfo customerInfo;

    private CustomerAddress customerAddress;
}