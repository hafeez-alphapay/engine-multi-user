package com.alphapay.payEngine.common.otp.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "otp_details")
public class OtpDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @Version
    private Long version;

    @Column(name = "request_id", unique = true, nullable = false)
    private String requestId;

    @Column(name = "otp", nullable = false)
    private String otp;

    @Column(name = "tran_type")
    private String tranType;

    @Column(name = "last_generated")
    private Date lastGenerated;

    @Column(name = "expiry_time")
    private Date expiryTime;

    @Column(name = "generation_attempts")
    private int generationAttempts;

    @Column(name = "validation_attempts")
    private int validationAttempts;

    @Column(name = "is_validated")
    private boolean isValidated;

    public OtpDetails(String requestId, String otp, String tranType, Date lastGenerated, Date expiryTime, Integer generationAttempts, Integer validationAttempts) {
        this.requestId = requestId;
        this.otp = otp;
        this.tranType = tranType;
        this.lastGenerated = lastGenerated;
        this.expiryTime = expiryTime;
        this.generationAttempts = generationAttempts;
        this.validationAttempts = validationAttempts;
    }
}
