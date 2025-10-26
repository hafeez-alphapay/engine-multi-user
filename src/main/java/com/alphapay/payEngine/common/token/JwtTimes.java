package com.alphapay.payEngine.common.token;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public record JwtTimes(Instant iat, Instant exp) {
    public static JwtTimes parse(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid JWT");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        try {
            var node = new ObjectMapper().readTree(payloadJson);
            long iat = node.path("iat").asLong(); // seconds since epoch (UTC)
            long exp = node.path("exp").asLong();
            if (exp == 0) throw new IllegalArgumentException("JWT has no exp");
            return new JwtTimes(Instant.ofEpochSecond(iat), Instant.ofEpochSecond(exp));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT times", e);
        }
    }
}
