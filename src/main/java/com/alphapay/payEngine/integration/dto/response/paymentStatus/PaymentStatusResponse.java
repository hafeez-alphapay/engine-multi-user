package com.alphapay.payEngine.integration.dto.response.paymentStatus;

import com.alphapay.payEngine.utilities.BigDecimalCommaDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class PaymentStatusResponse {

    @JsonProperty("IsSuccess")
    private boolean isSuccess;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("ValidationErrors")
    private List<ValidationError> validationErrors;

    @JsonProperty("Data")
    private PaymentData data;

    private String redirectUrl;

    @Getter
    @Setter
    @ToString
    public static class ValidationError {

        @JsonProperty("PropertyName")
        private String propertyName;

        @JsonProperty("PropertyValue")
        private String propertyValue;

        @JsonProperty("Message")
        private String message;
    }

    @Getter
    @Setter
    @ToString
    public static class PaymentData {

        @JsonProperty("InvoiceId")
        private String invoiceId;

        @JsonProperty("InvoiceStatus")
        private String invoiceStatus;

        @JsonProperty("InvoiceReference")
        private String invoiceReference;

        @JsonProperty("CustomerReference")
        private String customerReference;

        @JsonProperty("CreatedDate")
        private Date createdDate;

        @JsonProperty("ExpiryDate")
        private String expiryDate;

        @JsonProperty("ExpiryTime")
        private String expiryTime;

        @JsonProperty("InvoiceValue")
        @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
        private BigDecimal invoiceValue;

        @JsonProperty("Comments")
        private String comments;

        @JsonProperty("CustomerName")
        private String customerName;

        @JsonProperty("CustomerMobile")
        private String customerMobile;

        @JsonProperty("CustomerEmail")
        private String customerEmail;

        @JsonProperty("UserDefinedField")
        private String userDefinedField;

        @JsonProperty("InvoiceDisplayValue")
        private String invoiceDisplayValue;

        @JsonProperty("DueDeposit")
        @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
        private BigDecimal dueDeposit;

        @JsonProperty("DepositStatus")
        private String depositStatus;

        @JsonProperty("InvoiceItems")
        private List<InvoiceItem> invoiceItems;

        @JsonProperty("InvoiceTransactions")
        private List<InvoiceTransaction> invoiceTransactions;

        @JsonProperty("Suppliers")
        private List<Supplier> suppliers;

        @Getter
        @Setter
        @ToString
        public static class InvoiceItem {

            @JsonProperty("ItemName")
            private String itemName;

            @JsonProperty("Quantity")
            private int quantity;

            @JsonProperty("UnitPrice")
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal unitPrice;
        }

        @Getter
        @Setter
        @ToString
        public static class InvoiceTransaction {

            @JsonProperty("TransactionDate")
            private Date transactionDate;

            @JsonProperty("PaymentGateway")
            private String paymentGateway;

            @JsonProperty("ReferenceId")
            private String referenceId;

            @JsonProperty("TrackId")
            private String trackId;

            @JsonProperty("TransactionId")
            private String transactionId;

            @JsonProperty("PaymentId")
            private String paymentId;

            @JsonProperty("AuthorizationId")
            private String authorizationId;

            @JsonProperty("TransactionStatus")
            private String transactionStatus;

            @JsonProperty("TransationValue")
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal transationValue;

            @JsonProperty("CustomerServiceCharge")
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal customerServiceCharge;

            @JsonProperty("TotalServiceCharge")
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal totalServiceCharge;

            @JsonProperty("DueValue")
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal dueValue;

            @JsonProperty("PaidCurrency")
            private String paidCurrency;

            @JsonProperty("PaidCurrencyValue")
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal paidCurrencyValue;

            @JsonProperty("VatAmount")
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal vatAmount;

            @JsonProperty("IpAddress")
            private String ipAddress;

            @JsonProperty("Country")
            private String country;

            @JsonProperty("Currency")
            private String currency;

            @JsonProperty("Error")
            private String error;

            @JsonProperty("CardNumber")
            private String cardNumber;

            @JsonProperty("ErrorCode")
            private String errorCode;

            @JsonProperty("ECI")
            private String eci;
        }

        @Getter
        @Setter
        @ToString
        public static class Supplier {

            @JsonProperty("SupplierCode")
            private int supplierCode;

            @JsonProperty("SupplierName")
            private String supplierName;

            @JsonProperty("InvoiceShare")
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal invoiceShare;

            @JsonProperty("ProposedShare")
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal proposedShare;

            @JsonProperty("DepositShare")
            @JsonDeserialize(using = BigDecimalCommaDeserializer.class)
            private BigDecimal depositShare;
        }
    }
}