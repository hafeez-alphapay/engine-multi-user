package com.alphapay.payEngine.utilities;

import lombok.Getter;

@Getter
public enum InvoiceStatus {
    PENDING("Pending"),
    ACTIVE("Active"),
    PAID("Paid"),
    FAILED("Failed"),
    EXPIRED("Expired");

    private final String status;

    InvoiceStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }

    public static InvoiceStatus fromString(String status) {
        for (InvoiceStatus invoiceStatus : InvoiceStatus.values()) {
            if (invoiceStatus.getStatus().equalsIgnoreCase(status)) {
                return invoiceStatus;
            }
        }
        throw new IllegalArgumentException("No constant with status " + status + " found");
    }
}