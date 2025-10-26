package com.alphapay.payEngine.account.roles.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class UserNotFoundException extends BaseWebApplicationException {

    public UserNotFoundException() {
        super(
                404,
                "7210",
                "ex.7210.user.not.found",
                "User Not Found",
                "The specified user does not exist."
        );
    }
}