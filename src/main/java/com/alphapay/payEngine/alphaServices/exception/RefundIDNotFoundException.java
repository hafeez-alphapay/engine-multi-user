package com.alphapay.payEngine.alphaServices.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class RefundIDNotFoundException extends BaseWebApplicationException {
    public RefundIDNotFoundException() {
        super(
                400,
                "878",
                "ex.878.refund.not.found",
                "Refund ID is not Found.",
                "Refund ID is not Found.");
    }
}
