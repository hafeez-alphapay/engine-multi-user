package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.model.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class CreateSupplier extends BaseResponse {
    private String supplierName;
    private String mobile;
    private String email;
    private boolean isPercentageOfNetValue;
    private BigDecimal commissionValue;
    private BigDecimal commissionPercentage;
    private String depositTerms;
    private String depositDay;
    private boolean displaySupplierDetails;
    private Long merchantId;
    private int bankId;
    private String bankAccountHolderName;
    private String bankAccount;
    private String iban;
    private boolean isActive;
    private LogoFile logoFile;
    private String businessName;
    private int businessType;
    Map<String, Object> responseData = new HashMap<>();

}