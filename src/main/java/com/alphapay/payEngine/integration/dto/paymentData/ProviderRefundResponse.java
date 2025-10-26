package com.alphapay.payEngine.integration.dto.paymentData;

import lombok.Data;

import java.util.List;

@Data
public class ProviderRefundResponse {

    private String status;
    private int responseCode;
    private String responseMessage;
    private String merchantId;
    private String paymentId;
    private ResponseData responseData;

    @Data
    public static class ResponseData {
        private List<RefundResult> refunds_result;
    }

    @Data
    public static class RefundResult {
        private String refund_reference_id;
        private String refund_status_message;
        private String refund_result;
        private String refund_amount;
    }
}

