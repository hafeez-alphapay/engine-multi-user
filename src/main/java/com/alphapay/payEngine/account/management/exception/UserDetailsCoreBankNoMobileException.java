package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class UserDetailsCoreBankNoMobileException extends BaseWebApplicationException {
    public UserDetailsCoreBankNoMobileException() {
        super(409, "1502", "ex.1502.account.update.mobileno.imal", "Your mobile is not registered, kindly contact the bank", "Your mobile is not registered, kindly contact the bank");
    }
}
