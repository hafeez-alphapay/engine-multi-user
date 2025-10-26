package com.alphapay.payEngine.account.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "financial_institutions")
@ToString
public class FinancialInstitutions implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "application_id", unique = true, nullable = false)
    private String applicationId;
    @Column(name = "name", nullable = false)
    private String name;
    private String defaultOTPChannel;

    private Boolean smsNotificationEnabled;

    private Boolean loginTokenRequiredForAPIsAccess;

    private Boolean whatsappNotificationEnabled;
    private Boolean emailNotificationEnabled;
    private Boolean pushNotificationEnabled;
    private Boolean transactionNotificationEnabled;
    private String version;

    private String allowedTransferAccountTypes;
    private String allowedChequeRequestAccountTypes;
    private String allowedTransferCurrencies;
    private String allowedPaymentCurrencies;
    private String allowedOnboardingNewAccountTypes;

    private Boolean fetchFromAccountData;

    private String inActiveAccountStatus;

    private Boolean disableResetFromLockedUser;

    private Boolean fetchUserDetailsToRequest;

    //if false return matching Beneficiary account
    private Boolean returnAllBeneficiaryAccounts;

    private Boolean uniqueDeviceIdPerUserRestriction;



}
