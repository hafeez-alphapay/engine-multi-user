package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class DuplicateEntryException extends BaseWebApplicationException {

    public DuplicateEntryException() {
        super(409, "2200", "ex.2200.duplicate.entry", "Duplicate Entry", "Unique Constraint Violation.");
    }

    public DuplicateEntryException(String applicationMessage) {
        super(409, "2200", "ex.2200.duplicate.entry", "Duplicate Entry", applicationMessage);
    }

    public DuplicateEntryException(String errorCode, String errorMessageKey) {
        super(409, errorCode, errorMessageKey, "Duplicate Entry", null);
    }
}
