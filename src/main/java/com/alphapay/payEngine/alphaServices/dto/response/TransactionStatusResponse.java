package com.alphapay.payEngine.alphaServices.dto.response;

import com.alphapay.payEngine.integration.dto.response.RefundQueryResponse;
import com.alphapay.payEngine.model.response.BaseResponse;
import com.alphapay.payEngine.utilities.BigDecimalCommaDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionStatusResponse extends BaseResponse {
    private String redirectUrl;
    private ResponseData responseData;

    private RefundQueryResponse.RefundQueryData refundData;

    private String requestType="Update_Payment_Status";

    @Setter
    @Getter
    @ToString
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseData {
        private String invoiceId;
        private String invoiceTitle;
        private String invoiceDescription;
        private String invoiceStatus;
        private String invoiceReference;
        private String customerReference;
        private String createdDate;
        private String expiryDate;
        private String expiryTime;
        private double invoiceValue;
        private String comments;
        private String customerName;
        private String customerMobile;
        private String customerEmail;
        private String invoiceDisplayValue;
        private double dueDeposit;
        private String depositStatus;
        private List<InvoiceItem> invoiceItems;
        private List<InvoiceTransaction> invoiceTransactions;
        private List<Supplier> suppliers;
        private boolean signatureRequired;
        private String signatureUrl;
        private Map<String, Object> additionalInputs;
        private Map<String, Object> additionalOutputs;



        // Add toFlatMap method at the bottom of ResponseData
        public Map<String, String> toFlatMap() {
            Map<String, String> map = new HashMap<>();
            map.put("invoiceId", invoiceId);
            map.put("invoiceStatus", invoiceStatus);
            map.put("invoiceReference", invoiceReference);
            map.put("customerReference", customerReference);
            map.put("createdDate", createdDate);
            map.put("expiryDate", expiryDate);
            map.put("expiryTime", expiryTime);
            map.put("invoiceValue", String.valueOf(invoiceValue));
            map.put("comments", comments);
            map.put("customerName", customerName);
            map.put("customerMobile", customerMobile);
            map.put("customerEmail", customerEmail);
            map.put("invoiceDisplayValue", invoiceDisplayValue);
            map.put("dueDeposit", String.valueOf(dueDeposit));
            map.put("depositStatus", depositStatus);
            if(suppliers != null && !suppliers.isEmpty()) {
            map.put("supplierCode", String.valueOf(suppliers.get(0).getSupplierCode()));
            map.put("supplierName", suppliers.get(0).getSupplierName());
            map.put("invoiceShare", String.valueOf(suppliers.get(0).getInvoiceShare()));
            map.put("proposedShare", String.valueOf(suppliers.get(0).getProposedShare()));
            map.put("depositShare", String.valueOf(suppliers.get(0).getDepositShare()));}

            return map;
        }

        @Getter
        @Setter
        @ToString
        public static class InvoiceItem {
            private String itemName;
            private int quantity;
            private BigDecimal unitPrice;
        }

        @Setter
        @Getter
        @ToString
        public static class InvoiceTransaction {

            private Date transactionDate;
            private String paymentGateway;
            private String referenceId;
            private String trackId;
            private String transactionId;
            private String paymentId;
            private String authorizationId;
            private String transactionStatus;
            private String ipAddress;
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal transactionValue;
            private BigDecimal customerServiceCharge;
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal totalServiceCharge;
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal dueValue;
            private BigDecimal vatAmount;
            private String paidCurrency;
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal paidCurrencyValue;
            private String country;
            private String currency;
            private String errorMessage;
            private String errorCode;
            private String cardNumber;

        }

        @Getter
        @Setter
        @ToString
        public static class Supplier {

            private int supplierCode;

            private String logo;
            private String supplierName;

            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal invoiceShare;

            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal proposedShare;

            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal depositShare;
        }
    }
}