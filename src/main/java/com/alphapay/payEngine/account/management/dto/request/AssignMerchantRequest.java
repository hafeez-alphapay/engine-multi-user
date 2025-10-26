package com.alphapay.payEngine.account.management.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AssignMerchantRequest extends BaseRequest {
    private Long merchantId;
    private Long selectedId;
}
