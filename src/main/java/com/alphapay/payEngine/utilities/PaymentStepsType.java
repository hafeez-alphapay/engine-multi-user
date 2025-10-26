package com.alphapay.payEngine.utilities;

import lombok.Getter;

@Getter
public enum PaymentStepsType {
    INITIATE_PAYMENT_S2("InitiatePaymentRequest"),
    INVOICE_SUMMARY_S1("InvoiceSummaryRequest"),
    EXECUTE_PAYMENT_S3("ExecutePaymentRequest"),
    MAKE_REFUND("MakeRefundRequest");

    private final String name;

    PaymentStepsType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static PaymentStepsType fromString(String name) {
        for (PaymentStepsType productType : PaymentStepsType.values()) {
            if (productType.getName().equalsIgnoreCase(name)) {
                return productType;
            }
        }
        throw new IllegalArgumentException("No constant with name " + name + " found");
    }
}