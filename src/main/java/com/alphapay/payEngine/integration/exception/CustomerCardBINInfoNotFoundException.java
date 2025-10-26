package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class CustomerCardBINInfoNotFoundException extends BaseWebApplicationException {
    public CustomerCardBINInfoNotFoundException() {
        super(
                404,
                "104",
                "ex.104.customer.card.bin.info.not.found",
                "Customer card bin info not found.",
                "Customer  card bin info not found"
        );
    }
}