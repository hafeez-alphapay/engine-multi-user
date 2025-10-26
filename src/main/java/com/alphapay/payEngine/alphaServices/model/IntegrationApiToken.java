package com.alphapay.payEngine.alphaServices.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "api_tokens")
@Getter
@Setter
public class IntegrationApiToken {
    @Id private String tokenName;
    @Lob private String accessToken;
    private Instant issuedAtUtc;
    private Instant expiresAtUtc;
    private Instant lastUpdatedUtc;
    @Version private long version;
    private String workFlowId;
    private String userName;
    private String password;

    @Override
    public String toString() {
        return "IntegrationApiToken{" +
                "tokenName='" + tokenName + '\'' +
                ", accessToken='" + "****" + '\'' +
                ", issuedAtUtc=" + issuedAtUtc +
                ", expiresAtUtc=" + expiresAtUtc +
                ", lastUpdatedUtc=" + lastUpdatedUtc +
                ", version=" + version +
                ", workFlowId='" + workFlowId +
                '}';
    }
}
