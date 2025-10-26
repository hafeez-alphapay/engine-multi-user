package com.alphapay.payEngine.integration.dto.paymentData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class InvoiceItemRequest {
    private String itemName; // ISBAN, or SKU
    private Integer quantity; // Item's quantity
    private BigDecimal unitPrice;
}
