package com.alphapay.payEngine.account.roles.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class PermissionDoesNotExistException extends BaseWebApplicationException {

    public PermissionDoesNotExistException() {
        super(
                403, // HTTP status code
                "7204", // Custom error code
                "ex.7204.permission.not.found", // Message key for localization
                "Permission Does Not Exist", // Default (developer-facing) error message
                "The specified permission does not exist in the system." // User-friendly error message
        );
    }
}
