package com.alphapay.payEngine.transactionLogging;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class DuplicateTransactionException extends BaseWebApplicationException {
    public DuplicateTransactionException() {
        super(409, "2203", "ex.2203.duplicate.incomin.tran", "requestId is duplicated", "requestId is duplicated,");
    }
}
