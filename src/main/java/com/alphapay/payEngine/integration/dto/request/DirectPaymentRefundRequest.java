package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.integration.dto.paymentData.BaseFinancialRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DirectPaymentRefundRequest extends BaseFinancialRequest {
    @NotBlank(message = "apiKey is required")
    private String apiKey;
    @NotBlank(message = "paymentId is required")
    private String paymentId;

    private String webhookUrl;


    private String comment;

    private BigDecimal amount;

    private String currency;
}
