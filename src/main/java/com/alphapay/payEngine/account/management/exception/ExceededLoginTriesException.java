package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class ExceededLoginTriesException extends BaseWebApplicationException {

    public ExceededLoginTriesException() {
        super(403, "7207", "ex.7207.login.exceededTryCount", "Exceeded maximum allowed login tries", "Exceeded maximum allowed login tries");
    }
}
