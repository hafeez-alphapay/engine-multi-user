package com.alphapay.payEngine.account.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CountriesResponse {
    private Long id;
    private String nameEn;
    private String nameAr;
    private String iosCode;
}