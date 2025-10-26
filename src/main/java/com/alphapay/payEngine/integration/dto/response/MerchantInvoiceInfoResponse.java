package com.alphapay.payEngine.integration.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchantInvoiceInfoResponse {
    private String fullName;
    private String legalName;
    private String tradeNameEnglish;
    private String businessAddress;
    private String websiteUrl;
    private String socialMediaUrl;
    private String ownerMobileNumber;
    private String ownerEmail;
    private String businessLogo;
}
