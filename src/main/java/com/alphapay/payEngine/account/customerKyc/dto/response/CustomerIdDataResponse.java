package com.alphapay.payEngine.account.customerKyc.dto.response;

import lombok.Data;

@Data
public class CustomerIdDataResponse {
    private int id;
    private Passport passport;
    private MrzData mrz_data;
    private String uuid;
}