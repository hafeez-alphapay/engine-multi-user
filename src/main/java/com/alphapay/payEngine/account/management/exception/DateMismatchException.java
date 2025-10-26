package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class DateMismatchException extends BaseWebApplicationException {

    public DateMismatchException() {
        super(400, "4203", "ex.4203.validation.dateMismatch", "toDate can't be before fromDate", "toDate can't be before fromDate");
    }
}
