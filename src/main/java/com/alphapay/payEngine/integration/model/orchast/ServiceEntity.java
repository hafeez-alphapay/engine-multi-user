package com.alphapay.payEngine.integration.model.orchast;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceEntity extends CommonBean {

    @Column(name = "service_id", nullable = false)
    private String serviceId;

    @Column(name = "endpoint", nullable = false)
    private String endpointUrl;

    @Column(name = "json_header", columnDefinition = "JSON")
    private String jsonHeader;

    @Column(name = "request_mapper", columnDefinition = "JSON")
    private String requestMapper;

    @Column(name = "response_mapper", columnDefinition = "JSON")
    private String responseMapper;

    @Column(name = "error_response_mapper", columnDefinition = "JSON")
    private String errorResponseMapper;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "encryption_key")
    private String apiEncryptionKey;

    @Column(name = "auth_prefix")
    private String authPrefix;

    @Column(name = "is_request_encrypted_body")
    private Boolean isRequestEncryptedBody;

    @Column(name = "is_response_encrypted_body")
    private Boolean isResponseEncryptedBody;

}
