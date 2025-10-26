package com.alphapay.payEngine.integration.dto.paymentData;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutePaymentRequest extends BaseFinancialRequest {
    @NotBlank
    private String paymentId;
    private String apiKey;

    private BigDecimal invoiceValue;
    @NotBlank
    private String paymentMethodId;

    private String customerName; // optional
    private String displayCurrencyIso; // optional
    private String mobileCountryCode; // optional
    private String customerMobile; // optional
    private String customerEmail; // optional
    private String callBackUrl; // optional
    private String errorUrl; // optional
    private String language; // optional (en/ar)
    private String customerReference; // optional
    private String customerCivilId; // optional
    private String userDefinedField; // optional
    private String expiryDate; // optional
    private String webhookUrl; // optional
    private CustomerAddressRequest customerAddress; // optional
    private List<InvoiceItemRequest> invoiceItems; // optional
    private List<Supplier> suppliers;
    private String sessionId;
    private Card card;
    private String paymentURL;
    private Boolean bypass3DS = false;
    private String paymentType = "card";
}
