package com.alphapay.payEngine.account.management.dto.request;


import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class MerchantAggregatorLinkRequest extends com.alphapay.payEngine.service.bean.BaseRequest {
    private Long merchantId;
    private Long aggregatorId;
    private String status;
}
