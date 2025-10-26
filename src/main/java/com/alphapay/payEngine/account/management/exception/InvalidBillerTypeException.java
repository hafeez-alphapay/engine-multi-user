package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class InvalidBillerTypeException extends BaseWebApplicationException {
    public InvalidBillerTypeException() {
        super(400, "4205", "ex.4205.validation.invalidBillerType", "Invalid Biller type", "Invalid Biller type");
    }
}
