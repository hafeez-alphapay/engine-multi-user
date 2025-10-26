package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.integration.dto.response.CustomerInfo;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GeneratePaymentGatewayInvoiceAndInitiatePaymentRequest extends GeneratePaymentGatewayInvoiceRequest {
    private String paymentId;
    private String callbackUrl;
    private String webhookUrl;
    private CustomerInfo customerInfo;
    private CustomerAddress customerAddress;
}
