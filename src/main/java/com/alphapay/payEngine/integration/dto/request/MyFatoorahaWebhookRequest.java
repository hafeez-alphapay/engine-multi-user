package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class MyFatoorahaWebhookRequest extends BaseRequest {

    private int eventType;

    private String event;

    private String requestId;

    private String dateTime;

    private String countryIsoCode;

    private EventData data;

    @Getter
    @Setter
    @ToString
    public static class EventData {

        private Long invoiceId;

        private String invoiceReference;

        private String createdDate;

        private String customerReference;

        private String customerName;

        private String customerMobile;

        private String customerEmail;

        private String transactionStatus;

        private String paymentMethod;

        private String userDefinedField;

        private String referenceId;

        private String trackId;

        private String paymentId;

        private String authorizationId;

        private String invoiceValueInBaseCurrency;

        private String baseCurrency;

        private String invoiceValueInDisplayCurrency;

        private String displayCurrency;

        private BigDecimal invoiceValueInPayCurrency;

        private String payCurrency;

        private Long refundId;

        private String refundReference;

        private String refundStatus;

        private String amount;

        private String comments;

        private Integer supplierCode;

        private String supplierName;

        private String supplierMobile;

        private String supplierEmail;

        private String supplierStatus;

        private KycFeedback kycFeedback;

        private GatewayReference gatewayReference;

        @Getter
        @Setter
        @ToString
        public static class KycFeedback {
            private String comments;

            private String rejectReasons;
        }

        @Getter
        @Setter
        @ToString
        public static class GatewayReference {
            private String authorizationId;

            private String paymentId;

            private String referenceId;

            private BigDecimal refundAmount;

            private String transactionId;

            private String currency;

            private String paymentMethod;
        }
    }
}