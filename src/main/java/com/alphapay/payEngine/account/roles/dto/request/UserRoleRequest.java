package com.alphapay.payEngine.account.roles.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRoleRequest extends BaseRequest {
    private Long userId;
    private Long roleId;
}
