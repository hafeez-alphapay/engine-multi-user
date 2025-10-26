package com.alphapay.payEngine.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

public final class JsonNodeUtil {
    private JsonNodeUtil() {}
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Best-effort conversion of a JsonNode to String; returns null on failure. */
    public static String safeToString(JsonNode node, boolean pretty) {
        if (node == null || node.isNull()) return null;

        try {
            // Scalars → readable text
            if (node.isTextual())  return node.asText();
            if (node.isNumber())   return node.numberValue().toString();
            if (node.isBoolean())  return String.valueOf(node.booleanValue());

            // Binary → short base64 summary (avoid huge logs)
            if (node.isBinary()) {
                try {
                    byte[] data = node.binaryValue();
                    String b64 = Base64.getEncoder().encodeToString(data);
                    return "[binary:" + data.length + "B] " + (b64.length() > 60 ? b64.substring(0, 60) + "…" : b64);
                } catch (Exception ignored) {
                    // fall through to JSON serialization
                }
            }

            // Objects/arrays → JSON
            return pretty
                    ? MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node)
                    : MAPPER.writeValueAsString(node);

        } catch (Exception e) {
            // Last resort
            try { return node.toString(); } catch (Throwable ignore) { return null; }
        }
    }
}