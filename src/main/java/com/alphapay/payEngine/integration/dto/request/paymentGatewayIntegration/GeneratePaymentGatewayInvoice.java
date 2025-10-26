package com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration;

import com.alphapay.payEngine.alphaServices.dto.request.GenerateInvoiceRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GeneratePaymentGatewayInvoice extends GenerateInvoiceRequest {
    @NotBlank(message = "apiKey is required")
    private String apiKey;

    @NotBlank(message = "service Id is required")
    private String serviceId;
}