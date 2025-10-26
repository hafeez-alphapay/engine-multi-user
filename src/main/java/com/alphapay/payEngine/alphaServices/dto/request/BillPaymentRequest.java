package com.alphapay.payEngine.alphaServices.dto.request;



import com.alphapay.payEngine.service.bean.BaseRequest;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class BillPaymentRequest extends BaseRequest {
    @NotNull(message = "Service ID can't be null")
    private String serviceId;

    private String clientTransactionReference;

    private String clientExternalAdditionalIdentifier;
    @NotNull(message = "Payment Inputs can't be null")
    private Map<String, Object> paymentInputs;

    /*
        Below are response fields from Core Bank and Syber Biller
     */
    //Core Bank Response Ref
    String transactionNumber;
    //Biller Responses
    private Map<String, Object> paymentResponse;

    @DecimalMin(value = "0", inclusive = true,message = "Amount  cant be less than 0")
    private BigDecimal amount;
    private String currency;

    private String operationType;


    // Response fields
    private BigDecimal equivalentAmount;

    private String equivalentCurrencyCode;


    private BigDecimal clientCommission;

}

