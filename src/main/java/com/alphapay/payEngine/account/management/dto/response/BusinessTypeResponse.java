package com.alphapay.payEngine.account.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessTypeResponse {
    private Long id;
    private String nameEn;
    private String nameAr;
    private List<BusinessCategoryResponse> categories;
}

