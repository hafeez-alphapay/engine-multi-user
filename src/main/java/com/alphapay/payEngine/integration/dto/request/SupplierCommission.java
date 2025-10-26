package com.alphapay.payEngine.integration.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SupplierCommission {
    private Long paymentMethodId;
    private BigDecimal commissionValue;
    private BigDecimal commissionPercentage;
    private boolean percentageOfNetValue;
}
