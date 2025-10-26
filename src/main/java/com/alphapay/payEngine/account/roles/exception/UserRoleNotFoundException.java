package com.alphapay.payEngine.account.roles.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class UserRoleNotFoundException extends BaseWebApplicationException {
    public UserRoleNotFoundException() {
        super(
                404,
                "7209",
                "ex.7214.user.role.not.found",
                "User Role Not Found",
                "User roles does not exist."
        );
    }
}
