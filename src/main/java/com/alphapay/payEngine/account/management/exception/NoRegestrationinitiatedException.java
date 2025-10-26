package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class NoRegestrationinitiatedException extends BaseWebApplicationException {

    public NoRegestrationinitiatedException() {
        super(409, "1202", "ex.1202.account.completionForNonExistingRequest", "No record for this registration id", "No record for this registration id");
    }

}
