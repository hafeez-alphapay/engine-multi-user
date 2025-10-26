package com.alphapay.payEngine.integration.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefundQueryResponse {
    private String requestId;
    private int responseCode;
    private String status;
    private String responseMessage;
    private List<RefundQueryData> responseData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RefundQueryData {
        private String currency;
        private BigDecimal amount;
        private String refundCurrency;
        private BigDecimal refundAmount;
        private String refundStatus;
        private String refundStatusMessage;
        private String refundId;
        private Long merchantId;
        private String invoiceLink;
        private String paymentId;
        private String comments;
        private String customerReference;
        private String transactionStatus;
    }
}