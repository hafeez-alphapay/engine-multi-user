package com.alphapay.payEngine.notification.exceptions;

import  com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class NotificationConfigDoesNotExistException extends BaseWebApplicationException {
    public NotificationConfigDoesNotExistException() {
        super(409,"3301", "ex.3301.notification.appid.config.notfound","Notification config with this application id does not exist","Notification config with this application id does not exist");
    }
}
