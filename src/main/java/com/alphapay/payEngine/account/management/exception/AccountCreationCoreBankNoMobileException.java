package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AccountCreationCoreBankNoMobileException extends BaseWebApplicationException {

    public AccountCreationCoreBankNoMobileException() {
        super(409, "1501", "ex.1501.account.creation.mobileno.imal", "Your mobile is not registered, kindly contact the bank", "Your mobile is not registered, kindly contact the bank");
    }
}
