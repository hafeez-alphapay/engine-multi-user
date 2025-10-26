package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class SupplierNotAssignedException extends BaseWebApplicationException {

    public SupplierNotAssignedException() {
        super(
                404,
                "7510",
                "ex.7510.merchant.not.registered",
                "Merchant Not registered on payment gateway",
                "Merchant Not registered on payment gateway"
        );
    }
}