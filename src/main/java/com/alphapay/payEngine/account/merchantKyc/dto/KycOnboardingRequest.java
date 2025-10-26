/**
 * DTO for onboarding request (updated to match agreed contract)
 */
package com.alphapay.payEngine.account.merchantKyc.dto;

import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Map;

@Data
public class KycOnboardingRequest extends BaseRequest {

    @NotBlank(message = "Trade name in Arabic is required")
    private String tradeNameAr;

    @NotBlank(message = "Trade name in English is required")
    private String tradeNameEn;

    @NotBlank(message = "Legal name is required")
    private String legalName;

    @NotBlank(message = "Business type is required")
    private String businessType;

    private String otherBusinessType;

    @NotBlank(message = "Business category is required")
    private String businessCategory;
    @NotBlank(message = "userResidenceEmirate is required")
    private String userResidenceEmirate;

    @NotBlank(message = "userNationality is required")
    private String userNationality;

    private String otherBusinessCategory;

    @NotBlank(message = "Business activity is required")
    private String businessActivity;

    private String otherbusinessActivity;

    @NotBlank(message = "Commercial license number is required")
    private String commercialLicenseNumber;

    @NotNull(message = "Commercial license expiry date is required")
    private Date commercialLicenseExpiryDate;

    @NotBlank(message = "Business physical address is required")
    private String businessPhysicalAddress;

    @NotBlank(message = "Emirate is required")
    private String emirate;

    @NotBlank(message = "Business legal address is required")
    private String businessLegalAddress;

    @NotBlank(message = "Business phone number is required")
    private String businessPhoneNumber;

    @NotBlank(message = "Office email address is required")
    @Email(message = "Invalid office email format")
    private String officeEmailAddress;

    @NotBlank(message = "Bank ID is required")
    private Long bankId;

    @NotBlank(message = "Bank account name is required")
    private String bankAccountName;

    @NotBlank(message = "Bank account number is required")
    private String bankAccount;

    @NotBlank(message = "Bank IBAN is required")
    private String bankIban;

    @NotEmpty(message = "At least one required service must be specified")
    private List<@NotBlank(message = "Required service cannot be blank") String> requiredServices;

    private List<String> websiteUrls;
    private List<String> socialMediaUrls;

//    @NotEmpty(message = "At least one website URL must be provided")
//    private List<@NotBlank(message = "Website URL cannot be blank") String> websiteUrls;

    @NotNull(message = "UAE target is required")
    private Integer uaeTarget;

    @NotNull(message = "EU target is required")
    private Integer euTarget;

    @NotNull(message = "UK target is required")
    private Integer ukTarget;

    @NotNull(message = "US target is required")
    private Integer usTarget;

    @NotNull(message = "ROW target is required")
    private Integer rowTarget;

    @NotNull(message = "Average order price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Average order price must be positive")
    private BigDecimal avgOrderPrice;

    @NotNull(message = "Maximum order price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Maximum order price must be positive")
    private BigDecimal maxOrderPrice;

    @NotNull(message = "Monthly order count is required")
    private Integer noOfOrdersMonthly;

    @NotNull(message = "Monthly order volume is required")
    private BigDecimal volumeOfOrdersMonthly;

    @NotNull(message = "Annual income is required")
    private BigDecimal annualIncome;

    @NotNull(message = "Estimated card transaction volume is required")
    private BigDecimal estimatedCardTransVolume;

    @NotBlank(message = "Currently accept card payments is required")
    private String currentlyAcceptCardPayments;

//    @NotBlank(message = "Current payment gateway is required")
    private String currentPaymentGateway;

//    @NotBlank(message = "Accepted card types must be specified")
    private String acceptedCardTypes;

    @Valid
    @NotNull(message = "Card payment methods are required")
    private CardPaymentMethodsDto cardPaymentMethods;

    @NotNull(message = "Processing currencies must be specified")
    private Map<String, Boolean> processingCurrencies;

    @NotNull(message = "Settlement currencies must be specified")
    private Map<String, Boolean> settlementCurrencies;


    @NotNull(message = "Average turnover value is required")
    private BigDecimal avgTurnoverValue;

    @NotNull(message = "Average turnover count is required")
    private Integer avgTurnoverCount;

    @NotNull(message = "Refund value is required")
    private BigDecimal refundValue;

    @NotNull(message = "Refund count is required")
    private Integer refundCount;

    @NotNull(message = "Cashback value is required")
    private BigDecimal cashbackValue;

    @NotNull(message = "Cashback count is required")
    private Integer cashbackCount;

    @NotBlank(message = "User email is required")
    @Email(message = "Invalid user email format")
    private String userEmail;

    @NotBlank(message = "User password is required")
    private String userPassword;

    @NotBlank(message = "Confirm user password is required")
    private String confirmUserPassword;

    @Valid
    @NotEmpty(message = "At least one manager must be provided")
    private List<KycManagerDto> managersList;

    private Long merchantId;

}