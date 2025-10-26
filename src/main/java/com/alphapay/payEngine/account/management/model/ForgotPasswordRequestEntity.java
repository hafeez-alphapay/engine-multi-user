package com.alphapay.payEngine.account.management.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "forgot_password_request")
public class ForgotPasswordRequestEntity extends CommonBean {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "session_id", length = 200)
    private String sessionId;

    @Column(name = "password_changed_status", length = 32)
    private String passwordChangedStatus;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;
}
