package com.alphapay.payEngine.account.management.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "user_registration_progress")
public class UserRegistrationProgress extends CommonBean {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "email_verified", columnDefinition = "TINYINT DEFAULT 0")
    private Boolean emailVerified;

    @Column(name = "phone_verified", columnDefinition = "TINYINT DEFAULT 0")
    private Boolean phoneVerified;

    @Column(name = "kyc_completed", columnDefinition = "TINYINT DEFAULT 0")
    private Boolean kycCompleted;

    @Column(name = "address_provided", columnDefinition = "TINYINT DEFAULT 0")
    private Boolean addressProvided;

    @Column(name = "bank_info_added", columnDefinition = "TINYINT DEFAULT 0")
    private Boolean bankInfoAdded;

    @Column(name = "company_doc_added", columnDefinition = "TINYINT DEFAULT 0")
    private Boolean companyDocAdded;




}

