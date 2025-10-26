package com.alphapay.payEngine.account.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OTPValidationRequest extends com.alphapay.payEngine.service.bean.BaseRequest {
    @NotBlank(message = "OTP is required")
    private String otp;
    private String type; // email or mobile
    @NotBlank(message = "RegistrationId is required")
    private String registrationId;
    @NotBlank(message = "Generated Otp Id is required")
    private String generatedOtpId;
}
