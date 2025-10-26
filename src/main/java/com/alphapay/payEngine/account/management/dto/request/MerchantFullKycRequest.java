package com.alphapay.payEngine.account.management.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MerchantFullKycRequest extends com.alphapay.payEngine.service.bean.BaseRequest {
    @NotNull(message = "Merchant ID is required")
    private Long merchantId;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "IBAN is required")
    private String iban;

    @NotEmpty(message = "At least one service must be selected")
    private List<String> serviceRequired;

    @NotBlank(message = "Registered business name is required")
    private String registeredBusinessName;

    @NotBlank(message = "Business activity is required")
    private String businessActivity;

    @NotBlank(message = "Business model is required")
    private String businessModel; // e.g., B2B or B2C

    @NotBlank(message = "Bank account details are required")
    private String bankAccountDetails; // Corporate or Personal

    @NotBlank(message = "Monthly turnover is required")
    private String monthlyTurnover; // Expected/Approximate in AED

    @NotBlank(message = "Business ownership status is required")
    private String businessOwnershipStatus; // Sole owner/Partnership/LLC

    @NotBlank(message = "Maximum payment per transaction is required")
    private String maxPaymentPerTransaction; // Maximum payment in AED

    @NotBlank(message = "Minimum payment per transaction is required")
    private String minPaymentPerTransaction; // Minimum payment in AED

    @NotBlank(message = "Average payment per transaction is required")
    private String averagePaymentPerTransaction; // Average payment in AED

    @NotBlank(message = "Local to international payments ratio is required")
    private String localInternationalPaymentsRatio; // Percentage

    @NotBlank(message = "Target countries are required")
    @NotEmpty(message = "At least one country must be selected")
    private List<String> targetCountries; // Target countries excluding GCC

    @NotBlank(message = "Website or social media URL is required")
    @Pattern(regexp = "^(http|https)://.*$", message = "Invalid URL format")
    private String websiteSocialMediaUrl; // Website or Social Media URL

    @NotBlank(message = "Complete office address is required")
    private String completeOfficeAddress;

    @NotBlank(message = "Official email is required")
    @Email(message = "Invalid email format")
    private String officialEmail;

    @NotBlank(message = "Authorized signatory name is required")
    private String authorizedSignatoryName;

    @NotBlank(message = "Authorized contact number is required")
    @Pattern(regexp = "\\+\\d{1,3}\\d{10}", message = "Invalid phone number format. Use format +XXXXXXXXXXXXX")
    private String authorizedContactNumber;
}
