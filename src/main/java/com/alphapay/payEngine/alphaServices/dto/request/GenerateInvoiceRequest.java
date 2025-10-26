package com.alphapay.payEngine.alphaServices.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class GenerateInvoiceRequest extends PaymentLinkCreationRequest {
    @NotBlank(message = "Invoice reference is required")
    private String invoiceReference;
    private boolean customerKycRequired;
    private boolean signatureRequired;
    private String callbackUrl;
    private String webhookUrl;
    private Map<String,Object> additionalInputs;
}