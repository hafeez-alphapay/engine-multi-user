package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.integration.dto.paymentData.BaseFinancialRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class RefundRequest extends BaseFinancialRequest {
    @NotEmpty
    private String key;
    private String keyType = "PaymentId";
    private String comment;
    @NotNull
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero")
    private BigDecimal supplierDeductedAmount;

    Long processorId;

    Long merchantId;
    String merchantName;
}
