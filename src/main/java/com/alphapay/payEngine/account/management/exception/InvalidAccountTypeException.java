package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class InvalidAccountTypeException extends BaseWebApplicationException {

    public InvalidAccountTypeException() {
        super(400, "4204", "ex.4204.validation.invalidAccountType", "Invalid account type", "Invalid account type");
    }

}
