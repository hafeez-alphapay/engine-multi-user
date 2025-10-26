package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class NoRegistrationInitiatedException extends BaseWebApplicationException {

    public NoRegistrationInitiatedException() {
        super(409, "1203", "ex.1203.account.completionForNonExistingRequest", "No record for this registration id", "No record for this registration id");
    }

}
