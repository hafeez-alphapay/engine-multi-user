package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class PhoneNotVerifiedException extends BaseWebApplicationException {

    public PhoneNotVerifiedException() {
        super(409, "1206", "ex.1206.phone.number.not.verified", "Phone Number Not Verified", "Phone Number Not Verified");
    }

}
