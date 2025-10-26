package com.alphapay.payEngine.account.management.dto.request;

import com.alphapay.payEngine.account.management.model.MerchantRegistration;
import com.alphapay.payEngine.account.management.model.UserDetails;
import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MerchantRegistrationRequest extends BaseRequest {
    // Business Information
    @NotBlank
    private String businessCategory;

    @NotBlank
    private String businessType;

    @NotBlank
    private String legalName; // Company name

    @NotBlank
    private String tradeNameEnglish; // Business name

    // Business Address
    @NotBlank
    private String emirate;

    @NotBlank
    private String businessAddress;

    private String websiteUrl;

    private String socialMediaUrl;

    // Owner Info
    @NotBlank
    @Pattern(regexp = "^\\+971\\d{8,9}$", message = "Invalid UAE mobile number")
    private String ownerMobileNumber;

    @Email
    @NotBlank
    private String ownerEmail;

    private String nationality;

    // Bank Account Details
    private String bankAccountName;

    private String bankName; // Drop-down list

    private String accountNumber;

    //    @Pattern(regexp = "^[A-Z]{2}\\d{2}[A-Z0-9]{1,30}$", message = "Invalid IBAN format")
    private String iban;

    // Manager User Login Information
    @NotBlank(message = "Full name cannot be blank")
    private String fullName;


    @NotBlank
    private String registrationId;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    public MerchantRegistration convertToEntity() {
        MerchantRegistration registration = new MerchantRegistration();

        UserDetails userDetails = new UserDetails();

        // Map fields from the request to UserDetails
        userDetails.setFullName(this.getFullName());

        userDetails.setPassword(this.getPassword());
        registration.setUserDetails(userDetails);
        registration.setEnabled(false);
        registration.setRequestId(this.getRequestId());
        registration.setApplicationId(this.getApplicationId());
        registration.setActivationDate(null);
        registration.setConfirmPassword(this.getConfirmPassword());
        registration.setRegistrationId(this.getRegistrationId());
        return registration;
    }
}
