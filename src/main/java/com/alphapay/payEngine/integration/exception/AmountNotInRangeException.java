package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class AmountNotInRangeException extends BaseWebApplicationException {
    public AmountNotInRangeException() {
        super(
                400,
                "7211",
                "ex.7211.role.already.assigned",
                "Role Already Assigned",
                "Role Already Assigned to user"
        );
    }
}
