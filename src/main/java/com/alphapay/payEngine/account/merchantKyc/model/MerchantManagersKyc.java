package com.alphapay.payEngine.account.merchantKyc.model;

import com.alphapay.payEngine.account.management.model.CountriesEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "merchant_managers_kyc")
@Getter
@Setter
public class MerchantManagersKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to parent profile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private MerchantEntity merchantEntity;

    private String fullName;

    private String idNumber;

    private Date idExpiry;

    private Date dob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality", referencedColumnName = "name_en", nullable = false)
    private CountriesEntity nationality;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String personType;

    private Boolean isShareholder;

    private String ownershipType;

    private Double ownershipPercentage;

    private String position;

    private String incomeSource;

    private String phone;

    private String email;

    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
    }
}
