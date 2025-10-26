package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class DuplicateEmailException extends BaseWebApplicationException {

    public DuplicateEmailException() {
        super(409, "2202", "ex.2202.duplicate.email", "Email Already Exists", "An attempt was made to create a user that already exists");
    }

    public DuplicateEmailException(String applicationMessage) {
        super(409, "2202", "ex.2202.duplicate.email", "Email Already Exists", applicationMessage);
    }

    public DuplicateEmailException(String errorCode, String errorMessage) {
        super(409, errorCode, "ex.2202.duplicate.email", errorMessage, null);
    }

    public DuplicateEmailException(String errorCode, String errorMessage, String applicationMessage) {
        super(409, errorCode, "ex.2202.duplicate.email", errorMessage, applicationMessage);
    }
}
