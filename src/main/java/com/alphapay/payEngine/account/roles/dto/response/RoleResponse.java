package com.alphapay.payEngine.account.roles.dto.response;

import com.alphapay.payEngine.model.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResponse extends BaseResponse {
    private Role role;
}
