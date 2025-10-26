package com.alphapay.payEngine.integration.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class VelocityTemplateEngine {
    private final VelocityEngine velocityEngine;
    private final ObjectMapper objectMapper;
    private final VelocityCustomTools velocityTools; // Add this line

    private static final Set<String> SENSITIVE_CARD_KEYS = Set.of(
            "executePayment_card_number",
            "executePayment_card_securityCode",
            "executePayment_card_expiryMonth",
            "executePayment_card_expiryYear",
            "executePayment_card_cardHolderName"
    );

    public VelocityTemplateEngine(ObjectMapper objectMapper) {
        this.velocityEngine = new VelocityEngine();
        this.velocityEngine.init();
        this.objectMapper = objectMapper;
        this.velocityTools = new VelocityCustomTools();
    }

    public String mergeTemplate(String templateContent, Map<String, Object> contextData) {
        try {
            Map<String, Object> flattenedContext = flattenMap(contextData);
            VelocityContext context = new VelocityContext();
            context.put("tools", velocityTools);
            flattenedContext.forEach(context::put);
            for (Map.Entry entry : flattenedContext.entrySet()) {
                context.put(entry.getKey().toString(), entry.getValue());
            }

            debugVelocityContext(context);
            StringWriter writer = new StringWriter();
            velocityEngine.evaluate(context, writer, "JSONMapper", templateContent);

            String result = writer.toString();
            // Clean JSON output
            result = result.replaceAll(",\\s*}", "}")
                    .replaceAll(",\\s*]", "]")
                    .replaceAll("(?m)^\\s+", ""); // Remove empty lines

            return result;
        } catch (Exception e) {
            log.error("Velocity template processing failed", e);
            throw new RuntimeException("Velocity template processing failed", e);
        }
    }

    private Map<String, Object> flattenMap(Map<String, Object> map) {
        Map<String, Object> flattened = new HashMap<>();
        flattenMap("", map, flattened);
        return flattened;
    }

    private void flattenMap(String prefix, Map<String, Object> source, Map<String, Object> target) {
        source.forEach((key, value) -> {
            String newKey = prefix.isEmpty() ? key : prefix + "_" + key;

            if (value instanceof Map) {
                flattenMap(newKey, (Map<String, Object>) value, target);
            } else if (value instanceof JsonNode) {
                // Handle JsonNode if needed
                target.put(newKey, value.toString());
            } else {
                target.put(newKey, value);
            }
        });
    }

    /**
     * Masks sensitive values for specific card-related keys.
     * For executePayment_card_number, keep only the last 4 digits and mask the rest with '*'.
     * For the other keys in SENSITIVE_CARD_KEYS, replace the entire value with ****.
     * Note: valueStr may already be quoted (e.g., "123"); this method preserves quotes.
     */
    private static String maskSensitiveValue(String key, String valueStr) {
        if (key == null || valueStr == null) return valueStr;

        if (!SENSITIVE_CARD_KEYS.contains(key)) {
            return valueStr; // not a sensitive card key
        }

        // Preserve surrounding quotes if present
        boolean quoted = valueStr.length() >= 2 && valueStr.startsWith("\"") && valueStr.endsWith("\"");
        String raw = quoted ? valueStr.substring(1, valueStr.length() - 1) : valueStr;

        if ("executePayment_card_number".equals(key)) {
            String last4 = raw.length() > 4 ? raw.substring(raw.length() - 4) : raw;
            String maskedDigits = "*".repeat(Math.max(0, raw.length() - last4.length())) + last4;
            return quoted ? '"' + maskedDigits + '"' : maskedDigits;
        }

        // For CVV, expiryMonth, expiryYear, cardHolderName -> mask entirely
        String masked = "****";
        return quoted ? '"' + masked + '"' : masked;
    }

    private void debugVelocityContext(VelocityContext context) {
        log.debug("===== Velocity Context Contents =====");

        // Get all keys from the context
        Object[] keys = context.getKeys();

        for (Object keyObj : keys) {
            String key = keyObj.toString();
            Object value = context.get(key);

            // Format the value for display
            String valueStr;
            if (value == null) {
                valueStr = "null";
            } else if (value instanceof String) {
                valueStr = "\"" + value + "\"";
            } else {
                valueStr = value.toString();
            }
            String maskedValue = maskSensitiveValue(key, valueStr);
            log.debug("{} = {} (Type: {})",
                    key,
                    maskedValue,
                    value != null ? value.getClass().getSimpleName() : "null");
        }

        log.debug("===== End of Context Contents =====");
    }
}