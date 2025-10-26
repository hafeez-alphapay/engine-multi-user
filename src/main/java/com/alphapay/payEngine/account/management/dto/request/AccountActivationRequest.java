package com.alphapay.payEngine.account.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AccountActivationRequest extends com.alphapay.payEngine.service.bean.BaseRequest {

    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @NotBlank(message = "Action is required (activate or deactivate)")
    private String action; // Values: "activate" or "deactivate"
}