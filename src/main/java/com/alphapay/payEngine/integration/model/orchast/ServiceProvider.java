package com.alphapay.payEngine.integration.model.orchast;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "service_provider")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceProvider extends CommonBean {

    @Column(name = "service_provider_name", nullable = false)
    private String serviceProviderName;

    @Column(name = "service_id", nullable = false, unique = true)
    private String serviceId;

    @Column(name = "auth_key", nullable = false, length = 1024)
    private String authKey;

    @Column(name = "base_url", nullable = false)
    private String baseUrl;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "webhook_url")
    private String webhookUrl;

    @Column(name = "callback_url")
    private String callbackUrl;

    @Column(name = "error_url")
    private String errorUrl;

    @Column(name = "timeout", nullable = false, columnDefinition = "INT DEFAULT 5000")
    private Integer timeout;

    @Column(name = "retry_count", nullable = false, columnDefinition = "INT DEFAULT 3")
    private Integer retryCount;

    @Column(name = "additional_config", columnDefinition = "JSON")
    private String additionalConfig;
}