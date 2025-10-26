package com.alphapay.payEngine.account.merchantKyc.model;

import com.alphapay.payEngine.account.management.model.BusinessCategoryEntity;
import com.alphapay.payEngine.account.management.model.BusinessTypeEntity;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "merchants")
@Getter
@Setter
public class MerchantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning user (matches merchants.owner_user_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private UserEntity ownerUser;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_merchant_id")
    private MerchantEntity parentMerchant;

    @JsonManagedReference
    @OneToMany(mappedBy = "parentMerchant", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MerchantEntity> subMerchants = new HashSet<>();

    // New fields
    private String status;
    private String tradeNameAr;
    private String tradeNameEn;
    private String legalName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_type", referencedColumnName = "name_en", nullable = true)
    private BusinessTypeEntity businessType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "other_business_type", referencedColumnName = "name_en", nullable = true)
    private BusinessTypeEntity otherBusinessType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_category", referencedColumnName = "name_en", nullable = true)
    private BusinessCategoryEntity businessCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "other_business_category", referencedColumnName = "name_en", nullable = true)
    private BusinessCategoryEntity otherBusinessCategory;
    private String businessActivity;
    private String otherBusinessActivity;

    private String commercialLicenseNumber;
    private java.sql.Date commercialLicenseExpiry;

    @Column(columnDefinition = "TEXT")
    private String businessLegalAddress;

    private String emirate;
    @Column(columnDefinition = "TEXT")
    private String businessPhysicalAddress;

    private String businessPhoneNumber;

    private String officeEmailAddress;

    private String bankName;
    private String bankAccountName;
    private String bankAccount;
    private String bankIban;

    @Column(columnDefinition = "TEXT")
    private String websiteUrls;

    @Column(columnDefinition = "TEXT")
    private String socialMediaUrls;

    @Column(columnDefinition = "TEXT")
    private String requiredServices; // JSON

    private String currentlyAcceptCardPayments;

    private String currentPaymentGateway;
    private String acceptedCardTypes;

    @Column(columnDefinition = "TEXT")
    private String cardPaymentMethods;

    @Column(columnDefinition = "TEXT")
    private String processingCurrencies;

    @Column(columnDefinition = "TEXT")
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

    private Integer uaeTarget;
    private Integer euTarget;
    private Integer ukTarget;
    private Integer usTarget;
    private Integer rowTarget;

    private Long billerClientId;
    private String billerClientStatus;
    private String logo;

    @Column(name = "is_restricted_countries")
    private Boolean isRestrictedCountries;
    @Column(name = "admin_approve_status", nullable = false)
    private String adminApproveStatus = "PENDING";

    @Column(name = "manager_approve_status", nullable = false)
    private String managerApproveStatus = "PENDING";

    @Column(name = "mbme_approve_status", nullable = false)
    private String mbmeApproveStatus = "PENDING";

    @Column(name = "myfattora_approve_status", nullable = false)
    private String myfattoraApproveStatus = "PENDING";
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "merchantEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MerchantManagersKyc> managers;

    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }
}