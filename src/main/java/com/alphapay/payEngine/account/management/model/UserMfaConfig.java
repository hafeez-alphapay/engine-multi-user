package com.alphapay.payEngine.account.management.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_mfa_config")
@Getter
@Setter
public class UserMfaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String mfaType; // "TOTP"

    private String secret;

    private boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime verifiedAt;
}

