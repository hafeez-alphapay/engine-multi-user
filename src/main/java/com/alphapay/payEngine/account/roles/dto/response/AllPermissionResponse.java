package com.alphapay.payEngine.account.roles.dto.response;


import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AllPermissionResponse extends BaseResponse {
    private List<PermissionResponse> permissions;
}
