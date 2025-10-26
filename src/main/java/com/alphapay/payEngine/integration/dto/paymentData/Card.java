package com.alphapay.payEngine.integration.dto.paymentData;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Card {
    private String number;
    private String expiryMonth;
    private String expiryYear;
    private String securityCode;
    private String cardHolderName;

    @Override
    public String toString() {
        return "Card{" +
                "number='" + "***PROTECTED***" + '\'' +
                ", expiryMonth='" +  "***PROTECTED***" + '\'' +
                ", expiryYear='" +  "***PROTECTED***" + '\'' +
                ", securityCode='" +  "***PROTECTED***" + '\'' +
                ", cardHolderName='" +  "***PROTECTED***" + '\'' +
                '}';
    }
}