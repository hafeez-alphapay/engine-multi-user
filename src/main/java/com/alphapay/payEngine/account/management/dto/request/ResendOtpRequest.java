package com.alphapay.payEngine.account.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ResendOtpRequest extends com.alphapay.payEngine.service.bean.BaseRequest {
    @NotNull
    private String registrationId;
    private String type;
}
