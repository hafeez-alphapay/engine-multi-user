package com.alphapay.payEngine.account.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GenerateOtpRequest extends com.alphapay.payEngine.service.bean.BaseRequest {

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Contact is required")
    private String contact;

    private String countryCode;

    @NotBlank(message = "registrationId is required")
    private String registrationId;

}