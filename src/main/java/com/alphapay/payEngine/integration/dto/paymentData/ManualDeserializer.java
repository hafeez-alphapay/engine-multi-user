package com.alphapay.payEngine.integration.dto.paymentData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ManualDeserializer {

    public static ExecutePaymentResponse parseExecutePaymentResponse(String json) {
        ExecutePaymentResponse response = new ExecutePaymentResponse();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode root = objectMapper.readTree(json);

            response.setStatus(root.path("IsSuccess").asBoolean() ? "SUCCESS" : "FAILED");
            response.setResponseMessage(root.path("Message").asText(null));
            response.setValidationErrors(root.path("ValidationErrors").isNull() ? null : root.path("ValidationErrors").toString());

            JsonNode dataNode = root.path("Data");
            if (!dataNode.isMissingNode()) {
                ApiResponse.Data data = new ApiResponse.Data();
                data.setInvoiceId(String.valueOf(dataNode.path("InvoiceId").asLong()));
                data.setIsDirectPayment(dataNode.path("IsDirectPayment").asBoolean());
                data.setPaymentURL(dataNode.path("PaymentURL").asText(null));
                data.setCustomerReference(dataNode.path("CustomerReference").asText(null));
                data.setUserDefinedField(dataNode.path("UserDefinedField").asText(null));
//                data.setRecurringId(dataNode.path("RecurringId").asText(null));
                data.setPaymentId(extractPaymentIdFromUrl(data.getPaymentURL()));
                response.setResponseData(data);

                response.setPaymentURL(data.getPaymentURL());
                response.setExternalInvoiceId(data.getInvoiceId() != null ? String.valueOf(data.getInvoiceId()) : null);
                // Extract and set paymentId from PaymentURL
                response.setExternalPaymentId(extractPaymentIdFromUrl(data.getPaymentURL()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Utility method to extract paymentId from PaymentURL.
     */
    private static String extractPaymentIdFromUrl(String url) {
        try {
            String decodedUrl = java.net.URLDecoder.decode(url, java.nio.charset.StandardCharsets.UTF_8);
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("paymentId=([^&]+)");
            java.util.regex.Matcher matcher = pattern.matcher(decodedUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}