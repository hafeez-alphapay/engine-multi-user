package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.integration.exception.PaymentProcessorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class VelocityCustomTools {

    public String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public String formatDate(long date, String pattern) {
        Date datxe = new Date(date);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(datxe);
    }

    public String formatDateTime(String format) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return OffsetDateTime.now(ZoneOffset.UTC).format(formatter);
        } catch (Exception e) {
            log.error("Error formatting timestamp using pattern: {}", format, e);
            return null;
        }
    }

    /**
     * Cleans an error message string by removing leading/trailing square brackets,
     * double quotes inside the message, and trimming spaces.
     * Example: input ["uid" is required] returns uid is required.
     *
     * @param input the error message to clean
     * @return the cleaned error message
     */
    public String cleanErrorMessage(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String cleaned = input.trim();

        // 1. Remove leading/trailing [ ] if log accidentally wrapped it
        if (cleaned.startsWith("[")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("]")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        // 2. If it looks like a JSON fragment, strip braces but keep content
        cleaned = cleaned.replaceAll("[{}]", " ");

        // 3. Normalize colons -> keep them! Just ensure spacing is safe
        cleaned = cleaned.replaceAll("\\s*:\\s*", ": ");

        // 4. Remove control characters
        cleaned = cleaned.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        // 5. Escape for JSON context if needed
        cleaned = cleaned.replace("\\", "\\\\").replace("\"", "'");

        // 6. Collapse multiple spaces
        cleaned = cleaned.replaceAll("\\s{2,}", " ").trim();

        log.debug("cleanedMessage------------>{}", cleaned);
        return cleaned;
    }


    public String handleFailedStatus(Map<String, Object> responseMap) {
        try {
            Object statusCodeObj = responseMap.get("status_code");
            if (statusCodeObj instanceof Number && ((Number) statusCodeObj).intValue() != 0) {
                List<String> errors = new ArrayList<>();

                Map<String, Object> errorInfo = (Map<String, Object>) responseMap.get("error_info");
                if (errorInfo != null && errorInfo.get("error_message") instanceof List) {
                    List<?> messages = (List<?>) errorInfo.get("error_message");
                    for (Object msg : messages) {
                        errors.add(String.valueOf(msg));
                    }
                }

                StringBuilder msg = new StringBuilder();
                for (String e : errors) {
                    msg.append(" - ").append(e);
                }

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("errorCode", "5203");
                errorResponse.put("applicationMessage", msg.toString().trim());
                errorResponse.put("errorMessageEn", msg.toString().trim());
                errorResponse.put("errorMessageAr", msg.toString().trim());

                List<Map<String, String>> validationErrors = new ArrayList<>();
                for (String e : errors) {
                    validationErrors.add(Collections.singletonMap("name", e));
                }
                errorResponse.put("validationErrors", validationErrors);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode errorMessageNode = mapper.convertValue(errorResponse, JsonNode.class);
                log.debug("xerrorResponse:::{}", errorResponse);
                throw new PaymentProcessorException(errorMessageNode,
                        validationErrors.toArray(new Object[0]));

            }
            return null;
        } catch (Exception e) {
            return "{\"errorCode\":\"5203\",\"applicationMessage\":\"Invalid error format\"}";
        }
    }
}