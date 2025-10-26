package com.alphapay.payEngine.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.security.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailsDTO {
    private Long logId;
    private Long logInvoiceId;
    private String logPaymentId;
    private String type;
    private String description;
    private BigDecimal amount;
    private String paymentLinkTitle;
    private String currency;
    private String status;
    private Timestamp createdOn;
    private Timestamp expiryDate;
    private String customerName;
    private String customerContact;
}
