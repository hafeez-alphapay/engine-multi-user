package com.alphapay.payEngine.alphaServices.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_service_config", uniqueConstraints = {@UniqueConstraint(columnNames = "merchant_id")})
@Setter
@Getter
public class MerchantServiceConfigEntity extends CommonBean {
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "webhook_url", length = 250)
    private String webhookUrl;

    @Column(name = "webhook_secret_key", length = 250)
    private String webhookSecretKey;

    @Lob
    @Column(name = "api_key", columnDefinition = "LONGTEXT")
    private String apiKey;

    @Column(name = "callback_url", length = 250)
    private String callbackUrl;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
}
