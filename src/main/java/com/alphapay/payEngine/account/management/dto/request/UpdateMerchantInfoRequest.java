package com.alphapay.payEngine.account.management.dto.request;


import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateMerchantInfoRequest extends BaseRequest {

    @NotNull(message = "Merchant ID cannot be null")
    private Long merchantId;

    // Business Info
    @NotBlank(message = "Business category cannot be blank")
    private String businessCategory;


    @NotBlank(message = "Business type cannot be blank")
    private String businessType;

    @NotBlank(message = "Legal name cannot be blank")
    private String legalName;

    @NotBlank(message = "Trade name (English) cannot be blank")
    private String tradeNameEnglish;

    // Business Address
    @NotBlank(message = "Emirate cannot be blank")
    private String emirate;

    @NotBlank(message = "Business address cannot be blank")
    private String businessAddress;

    private String websiteUrl;

    private String socialMediaUrl;

    // Owner Info
    @Pattern(regexp = "^\\+971\\d{8,9}$", message = "Invalid UAE mobile number")
    private String ownerMobileNumber;

    @Email(message = "Invalid email format")
    private String ownerEmail;

    private String nationality;

    private String bankAccountName;

    private String bankName;

    private String accountNumber;

    private String iban;
    private boolean isCompany;
    private List<MerchantServiceRequest> merchantServices;
}