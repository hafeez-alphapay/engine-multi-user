package com.alphapay.payEngine.account.management.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
public class InvoiceStatusSummary {
    private String status;
    private long count;
    private BigDecimal totalAmount;

    public InvoiceStatusSummary(String status, long count, BigDecimal totalAmount) {
        this.status = status;
        this.count = count;
        this.totalAmount = totalAmount;
    }

}