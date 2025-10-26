package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class DuplicateUserException extends BaseWebApplicationException {

    public DuplicateUserException() {
        super(409, "2201", "ex.2201.duplicate.account", "Account already exists", "An attempt was made to create an account that already exists");
    }
    
    public DuplicateUserException(String applicationMessage) {
        super(409, "2201", "ex.2201.duplicate.account", "Account already exists", applicationMessage);
    }

    public DuplicateUserException(String errorCode, String errorMessageKey) {
        super(409, errorCode, errorMessageKey, "Account already exists", null);
    }
    public DuplicateUserException(String errorCode, String errorMessage, String applicationMessage) {
        super(409, errorCode, "ex.2201.duplicate.account", errorMessage, applicationMessage);
    }
}
