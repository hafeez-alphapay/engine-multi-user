package com.alphapay.payEngine.account.roles.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UpdateRoleRequest extends BaseRequest {
    private Long roleId;
    private String name;
    private List<Long> permissions;
}
