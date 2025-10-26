package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.integration.dto.paymentData.BaseFinancialRequest;
import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class PortalRefundRequest extends BaseFinancialRequest {
    @NotEmpty
    private String paymentId;
    private String comment;
    private String currency;
    private Long merchantId;
    @NotNull
    @DecimalMin(value = "0.1", inclusive = true, message = "Amount must be greater than zero")
    private BigDecimal amount;
}
