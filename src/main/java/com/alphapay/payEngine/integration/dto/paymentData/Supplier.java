package com.alphapay.payEngine.integration.dto.paymentData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class Supplier {
    private int supplierCode;

    private String supplierName;
    private BigDecimal invoiceShare;
    private BigDecimal proposedShare;
    private BigDecimal depositShare;
}
