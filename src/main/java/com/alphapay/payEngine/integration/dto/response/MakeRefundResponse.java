package com.alphapay.payEngine.integration.dto.response;

import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MakeRefundResponse extends BaseResponse {
    private ResponseData responseData;
    private String transactionStatus;
    private String transactionType;

    @Setter
    @Getter
    public static class ResponseData {
        private String key;
        private String refundId;
        private String refundReference;
        private String refundInvoiceId;
        private String amount;
        private String comment;
    }
}