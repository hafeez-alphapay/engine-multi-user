package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class DeviceIdException extends BaseWebApplicationException {

    public DeviceIdException() {
        super(409, "7203", "ex.7203.deviceid.mismatch", "You have changed the device you initially registered with", "You have changed the device you initially registered with");
    }
}
