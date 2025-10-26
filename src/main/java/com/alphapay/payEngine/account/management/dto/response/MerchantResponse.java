package com.alphapay.payEngine.account.management.dto.response;

import com.alphapay.payEngine.account.management.dto.request.MerchantServiceRequest;
import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@ToString
public class MerchantResponse extends BaseResponse {
    private Long id;
    private Long assignTo;
    private String fullName;
    private String countryCode;
    private String mobileNo;
    private String email;
    private String businessCategory;
    private String businessType;
    private String legalName;
    private String ownerMobileNumber;
    private String bankAccountName;
    private String bankName;
    private String accountNumber;
    private String iban;
    private boolean enabled;
    private boolean locked;

    private Date activationDate;
    private Date disabledDate;
    private List<MerchantServiceRequest> merchantServices;
    private List<MerchantManagersResponse> merchantManagersResponse;
    private String logo;
    private Long billerClientId;
    private String billerClientStatus;
    private Long subUserId;

    private String tradeNameAr;
    private String tradeNameEn;
    private String otherBusinessType;
    private String otherBusinessCategory;
    private String businessActivity;
    private String otherBusinessActivity;
    private String commercialLicenseNumber;
    private java.sql.Date commercialLicenseExpiry;
    private String businessLegalAddress;
    private String businessPhysicalAddress;
    private String emirate;
    private String businessPhoneNumber;
    private String officeEmailAddress;
    private String bankAccount;
    private String bankIban;
    private List<String> websiteUrls;
    private List<String> socialMediaUrls;
    private String requiredServices;
    private String currentlyAcceptCardPayments;
    private String currentPaymentGateway;
    private String acceptedCardTypes;
    private String cardPaymentMethods;
    private String processingCurrencies;
    private String settlementCurrencies;
    private BigDecimal avgOrderPrice;
    private BigDecimal maxOrderPrice;
    private Integer noOfOrdersMonthly;
    private BigDecimal volumeOfOrdersMonthly;
    private BigDecimal annualIncome;
    private BigDecimal estimatedCardTransVolume;
    private BigDecimal avgTurnoverValue;
    private Integer avgTurnoverCount;
    private BigDecimal refundValue;
    private Integer refundCount;
    private BigDecimal cashbackValue;
    private Integer cashbackCount;

    private Boolean isRestrictedCountries;
    private String adminApproveStatus;
    private String managerApproveStatus;
    private String mbmeApproveStatus;
    private String myfattoraApproveStatus;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
