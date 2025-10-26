package com.alphapay.payEngine.integration.dto.paymentData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    private String requestId;

    private String status;

    private int responseCode;

    private String responseMessage;

    @JsonProperty("ValidationErrors")
    private Object validationErrors;

    private Data responseData;
    private Map<String, String> paymentResponse;
    @JsonProperty("processorId")
    Long processorId;

    @Getter
    @Setter
    public static class Data {

        private String paymentId;

        private String invoiceId;

        private Boolean isDirectPayment;

        private String paymentURL;

        private String customerReference;

        private String userDefinedField;

        private String recurringId;

        private String status;

        private String errorCode;

        private String errorMessage;

        private String token;

        private String RecurringId;


        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();

            map.put("PaymentId", paymentId);
            map.put("InvoiceId", invoiceId);
            map.put("PaymentURL", paymentURL);
            map.put("CustomerReference", customerReference);
            map.put("UserDefinedField", userDefinedField);
            map.put("RecurringId", recurringId);
            map.put("Status", status);
            map.put("ErrorCode", errorCode);
            map.put("ErrorMessage", errorMessage);
            map.put("Token", token);
            map.put("RecurringId", RecurringId);

            return map;
        }


    }

}
