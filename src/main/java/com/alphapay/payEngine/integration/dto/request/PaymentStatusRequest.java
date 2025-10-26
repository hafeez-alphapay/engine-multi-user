package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentStatusRequest extends BaseRequest {

    private String paymentId;
    private String externalPaymentId;
    private String id;
    private String apiKey;
    private String keyType;
    private String serviceId;

    @AssertTrue(message = "Valid paymenId or externalPaymentId is required")
    public boolean isValidPaymentId() {
        if (paymentId == null || paymentId.isEmpty()) {
            if (externalPaymentId == null || externalPaymentId.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
