package com.alphapay.payEngine.account.roles.dto.response;

import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class AllRoleResponse extends BaseResponse {
    private List<Role> roles;
}
