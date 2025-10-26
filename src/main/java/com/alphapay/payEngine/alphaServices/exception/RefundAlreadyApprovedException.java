package com.alphapay.payEngine.alphaServices.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

import java.math.BigDecimal;

public class RefundAlreadyApprovedException extends BaseWebApplicationException {
    public RefundAlreadyApprovedException() {
        super(
                400,
                "877",
                "ex.877.refund.already.approved",
                "This Refund has already been approved and cannot be processed again.",
                "This Refund has already been approved and cannot be processed again.");
    }
}
