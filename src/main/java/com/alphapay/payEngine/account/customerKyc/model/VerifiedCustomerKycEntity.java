package com.alphapay.payEngine.account.customerKyc.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "verified_customer_kyc")
@Getter
@Setter
@ToString
public class VerifiedCustomerKycEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private String invoiceId;

    @Column(name = "passport_number", length = 50)
    private String passportNumber;

    @Column(name = "passport_image_url", columnDefinition = "TEXT")
    private String passportImageUrl;

    @Column(name = "valid_score")
    private Integer validScore;

    @Column(name = "document_type", length = 10)
    private String documentType;

    @Column(name = "issuing_country", length = 10)
    private String issuingCountry;

    @Column(name = "document_number", length = 50)
    private String documentNumber;

    @Column(name = "date_of_birth", length = 20)
    private String dateOfBirth;

    @Column(name = "expiration_date", length = 20)
    private String expirationDate;

    @Column(name = "nationality", length = 10)
    private String nationality;

    @Column(name = "sex", length = 1)
    private String sex;

    @Column(name = "names", length = 255)
    private String names;

    @Column(name = "surname", length = 255)
    private String surname;

    @Column(name = "is_valid_number")
    private Boolean isValidNumber;

    @Column(name = "valid_date_of_birth")
    private Boolean validDateOfBirth;

    @Column(name = "valid_expiration_date")
    private Boolean validExpirationDate;

    @Column(name = "valid_composite")
    private Boolean validComposite;

    @Column(name = "valid_personal_number")
    private Boolean validPersonalNumber;

    @Column(name = "uuid", length = 36)
    private String uuid;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
