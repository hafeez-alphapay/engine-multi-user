package com.alphapay.payEngine.integration.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerInfo {
    @NotBlank(message = "Customer Name is required")
    private String customerName;
    private String countryCode;
    private String customerContact;
    private String customerEmail;
    private String customerComment;
}
