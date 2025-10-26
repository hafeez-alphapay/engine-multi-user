package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.alphaServices.dto.request.GenerateInvoiceRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GeneratePaymentGatewayInvoiceRequest extends GenerateInvoiceRequest {
    @NotBlank(message = "apiKey is required")
    private String apiKey;
    @NotBlank(message = "service Id is required")
    private String serviceId;

    private String hash;
    @JsonIgnore //Internal use only
    private Long parentId;
}