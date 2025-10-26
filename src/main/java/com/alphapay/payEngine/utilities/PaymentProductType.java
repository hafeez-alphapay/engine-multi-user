package com.alphapay.payEngine.utilities;

import lombok.Getter;

@Getter
public enum PaymentProductType {
    PAYMENT_LINK("STANDARD"),
    INVOICE_LINK("INVOICE"),
    STATIC_QR_LINK("STATIC_QR"),
    PAYMENT_GATEWAY("PAYMENT_GATEWAY"),
    DIRECT_PAYMENT("DIRECT_PAYMENT");

    private final String name;

    PaymentProductType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static PaymentProductType fromString(String name) {
        for (PaymentProductType productType : PaymentProductType.values()) {
            if (productType.getName().equalsIgnoreCase(name)) {
                return productType;
            }
        }
        throw new IllegalArgumentException("No constant with name " + name + " found");
    }
}