package com.alphapay.payEngine.alphaServices.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class PaymentLinkCreationRequest extends BaseRequest {

    private String paymentLinkTitle;

    private String description;

    @DecimalMin(value = "0.00", message = "The amount must be greater than 0.0")
    @Digits(integer = 10, fraction = 2, message = "Amount can have up to 10 digits before and must have a maximum of 2 digits after the decimal point")
    private BigDecimal amount;

    @NotNull(message = "Please specify the currency")
    private String currency;

    private String customerName;

    private String language;

    private String customerContact;
    private String countryCode;
    private String customerEmail;
    private boolean openAmount;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private String comment;

    private boolean requiredTerms;

    private boolean sendEmail;

    private boolean sendSms;

    private String termsCondition;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date expiry;
    private Long merchantId;
    private List<InvoiceItemRequest> invoiceItems;
    private String paymentMethodsCode;
    private BigDecimal fixedDiscount;
    private int percentageDiscount;
    private int remindAfter;
    @PostConstruct
    private void defaultAmount() {
        if (this.amount == null  ) {
            this.amount = new BigDecimal(0);
        }
        if (this.minAmount == null  ) {
            this.minAmount = new BigDecimal(0);
        }
        if (this.maxAmount == null  ) {
            this.maxAmount = new BigDecimal(0);
        }
        if (this.fixedDiscount == null  ) {
            this.fixedDiscount = new BigDecimal(0);
        }
    }
}
