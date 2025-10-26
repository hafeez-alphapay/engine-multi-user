package com.alphapay.payEngine.account.roles.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Role {
    private Long roleId;
    private String name;
    private List<PermissionResponse> permissions;

}
