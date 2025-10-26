package com.alphapay.payEngine.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;



import java.util.Map;

@Service
public class SecretsManagerService {

    private final SecretsManagerClient secretsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecretsManagerService() {
        this.secretsClient = SecretsManagerClient.builder()
                .region(Region.of("me-central-1"))  // e.g. "eu-central-1"
                .build();
    }

    public Map<String, String> getSecret(String secretName) {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse response = secretsClient.getSecretValue(request);
        String secretJson = response.secretString();

        try {
            return objectMapper.readValue(secretJson, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse secrets JSON", e);
        }
    }

}