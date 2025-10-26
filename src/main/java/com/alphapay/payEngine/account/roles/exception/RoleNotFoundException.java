package com.alphapay.payEngine.account.roles.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class RoleNotFoundException extends BaseWebApplicationException {
    public RoleNotFoundException() {
        super(
                404,
                "7209",
                "ex.7209.role.not.found",
                "Role Not Found",
                "The specified role does not exist."
        );
    }
}
