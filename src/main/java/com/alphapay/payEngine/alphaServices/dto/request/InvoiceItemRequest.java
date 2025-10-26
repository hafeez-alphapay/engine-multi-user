package com.alphapay.payEngine.alphaServices.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class InvoiceItemRequest {
    private String name;
    private BigDecimal unitPrice;
    private Integer quantity = 1;
}