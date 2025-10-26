package com.alphapay.payEngine.integration.dto.paymentData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitiateSessionRequest extends BaseFinancialRequest {
    private String invoiceLink;
}
