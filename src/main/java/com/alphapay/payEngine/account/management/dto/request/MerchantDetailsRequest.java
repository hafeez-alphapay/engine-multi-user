package com.alphapay.payEngine.account.management.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MerchantDetailsRequest extends BaseRequest {
    private Long merchantId;
    private Long vendorMerchantId;
    private Long providerId;
    private Boolean isVendor = Boolean.TRUE;
}
