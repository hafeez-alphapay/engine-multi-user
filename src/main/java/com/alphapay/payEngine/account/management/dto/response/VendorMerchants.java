package com.alphapay.payEngine.account.management.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class VendorMerchants {
    private Long id;
    private String tradeNameEnglish;
    private List<SubMerchant> subMerchants;
}
