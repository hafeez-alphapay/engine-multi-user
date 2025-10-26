package com.alphapay.payEngine.account.roles.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class CreateRoleRequest extends BaseRequest {
    private String name;
    private List<Long> permissions;
}
