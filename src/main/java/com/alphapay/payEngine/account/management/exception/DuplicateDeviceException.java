package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class DuplicateDeviceException extends BaseWebApplicationException {

    public DuplicateDeviceException() {
        super(409, "2204", "ex.2204.duplicate.deviceId", "Device already registered", "An attempt was made to create an account with registered device");
    }

    public DuplicateDeviceException(String applicationMessage) {
        super(409, "2204", "ex.2204.duplicate.deviceId", "Device already registered", applicationMessage);
    }

    public DuplicateDeviceException(String errorCode, String errorMessageKey) {
        super(409, errorCode, errorMessageKey, "Device already registered", null);
    }
    public DuplicateDeviceException(String errorCode, String errorMessage, String applicationMessage) {
        super(409, errorCode, "ex.2204.duplicate.deviceId", errorMessage, applicationMessage);
    }
}
