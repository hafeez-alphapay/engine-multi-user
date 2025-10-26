package com.alphapay.payEngine.notification.exceptions;

import  com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class SMSConfigIsNullException extends BaseWebApplicationException {
    public SMSConfigIsNullException() {
        super(422,"3302","ex.3302.notification.sms.config.null","SMS gateway url, username or password is null","SMS gateway url, username or password is null");
    }
}
