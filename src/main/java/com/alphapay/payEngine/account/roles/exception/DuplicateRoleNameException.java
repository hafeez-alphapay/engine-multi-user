package com.alphapay.payEngine.account.roles.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class DuplicateRoleNameException extends BaseWebApplicationException {

    public DuplicateRoleNameException() {
        super(
                409,
                "7213",
                "ex.7213.role.duplicate.name",
                "Duplicate Role Name",
                "The role name already exists. Please choose a different name."
        );
    }
}
