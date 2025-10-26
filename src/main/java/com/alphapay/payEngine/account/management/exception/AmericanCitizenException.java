package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AmericanCitizenException extends BaseWebApplicationException {

    public AmericanCitizenException() {
        super(409, "7209", "ex.7209.american.citizen", "Based on your previous selection in American CitizenShip Section, you are not allowed to open online bank account bank will contact you soon.", "You are not allowed to open online bank account bank will contact you soon.");
    }
}
