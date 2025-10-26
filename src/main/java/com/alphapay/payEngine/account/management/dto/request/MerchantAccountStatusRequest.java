package com.alphapay.payEngine.account.management.dto.request;

import com.alphapay.payEngine.common.validator.ValidRoleForRequest;
import com.alphapay.payEngine.common.validator.ValidStatus;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class MerchantAccountStatusRequest extends com.alphapay.payEngine.service.bean.BaseRequest {

    private Long merchantId;


    @ValidRoleForRequest
    private String role;

    @ValidStatus
    private String newStatus;
}