package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AccountCreationCoreBankNoCoreBankEmailException extends BaseWebApplicationException {
    public AccountCreationCoreBankNoCoreBankEmailException() {
        super(409, "1503", "ex.1503.account.creation.corebankemail.imal", "Your email is not registered, kindly contact the bank", "Your email is not registered, kindly contact the bank");
    }
}
