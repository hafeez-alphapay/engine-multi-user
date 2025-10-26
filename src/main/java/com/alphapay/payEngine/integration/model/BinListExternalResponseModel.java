package com.alphapay.payEngine.integration.model;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BinListExternalResponseModel {
    private NumberInfo number;
    private String scheme;
    private String type;
    private String brand;
    private boolean prepaid;
    private CountryInfo country;
    private BankInfo bank;

    @Getter
    @Setter
    @ToString
    public static class NumberInfo {
        private int length;
        private boolean luhn;
    }

    @Getter
    @Setter
    @ToString
    public static class CountryInfo {
        private String numeric;
        private String alpha2;
        private String name;
        private String emoji;
        private String currency;
        private int latitude;
        private int longitude;
    }

    @Getter
    @Setter
    @ToString
    public static class BankInfo {
        private String name;
        private String url;
        private String phone;
        private String city;
    }
}
