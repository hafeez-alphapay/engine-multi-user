package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class MerchantIsNotAllowedForGW extends BaseWebApplicationException {
    public MerchantIsNotAllowedForGW() {
        super(
                404,
                "7513",
                "ِex.7513.merchant.not.allowed.for.gateway",
                "Merchant does not exist or is not allowed in this GW.",
                "Merchant does not exist or is not allowed in this GW."
        );
    }
}
