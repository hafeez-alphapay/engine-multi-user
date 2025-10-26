package com.alphapay.payEngine.integration.dto.paymentData;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class InitiateSessionResponse extends BaseFinancialRequest {

    private ResponseData responseData;

    @Setter
    @Getter
    public static class ResponseData {
        private String sessionId;
        private String countryCode;
    }
}