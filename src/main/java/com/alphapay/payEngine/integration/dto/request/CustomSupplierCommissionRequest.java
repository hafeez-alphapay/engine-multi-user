package com.alphapay.payEngine.integration.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class CustomSupplierCommissionRequest {
    private Long paymentMethodId;
    private BigDecimal commissionValue;
    private BigDecimal commissionPercentage;
    private boolean isPercentageOfNetValue;
}
