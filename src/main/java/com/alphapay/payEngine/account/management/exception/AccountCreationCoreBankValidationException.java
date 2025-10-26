package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AccountCreationCoreBankValidationException extends BaseWebApplicationException {

    public AccountCreationCoreBankValidationException() {
        super(409, "4501", "ex.4501.account.creation.notmatching.imal", "Probably you provided wrong number, if number is changed please contact the bank", "Probably you provided wrong number, if number is changed please contact the bank");
    }
}
