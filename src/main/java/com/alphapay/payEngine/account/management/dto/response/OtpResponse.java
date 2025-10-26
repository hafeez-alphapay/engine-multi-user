package com.alphapay.payEngine.account.management.dto.response;


import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class OtpResponse extends BaseResponse {
    private String registrationId;
    private String generatedOtpId;

//    public OtpResponse(String registrationId, String generatedOtpId, String requestId, String status, Integer responseCode, String responseMessage) {
//        super(requestId, status, responseCode, responseMessage);
//        this.registrationId = registrationId;
//        this.generatedOtpId = generatedOtpId;
//    }
}