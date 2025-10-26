package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class MBMEMerchantAlreadyAssignedException extends BaseWebApplicationException {

    public MBMEMerchantAlreadyAssignedException() {
        super(
                409,
                "7513",
                "ex.7513.merchant.registered.mbme",
                "Merchant Already registered on MBME",
                "Merchant Already registered on MBME"
        );
    }
}
