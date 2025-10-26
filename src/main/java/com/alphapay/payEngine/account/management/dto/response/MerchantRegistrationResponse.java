package com.alphapay.payEngine.account.management.dto.response;


import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@ToString(callSuper = true)
public class MerchantRegistrationResponse extends BaseResponse {
    private Map<String, Object> data;

    private Long merchantId;

    private String businessCategory;

    private String businessType;

    private String legalName;

    private String tradeNameEnglish;

    private String emirate;

    private String businessAddress;

    private String websiteUrl;

    private String socialMediaUrl;

    private String ownerMobileNumber;

    private String ownerEmail;

    private String nationality;

    private String bankAccountName;

    private String bankName;

    private String accountNumber;

    private String iban;
    private boolean isCompany;
    private List<Long> serviceIds;
}
