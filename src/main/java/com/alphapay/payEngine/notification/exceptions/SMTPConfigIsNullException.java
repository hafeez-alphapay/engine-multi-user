package com.alphapay.payEngine.notification.exceptions;

import  com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class SMTPConfigIsNullException extends BaseWebApplicationException {
    public SMTPConfigIsNullException() {
        super(422,"3303", "ex.3303.notification.smtp.config.null","SMTP config is null","SMTP config is null");
    }
}
