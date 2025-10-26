package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class BINNotAllowedToMerchantException extends BaseWebApplicationException {

    public BINNotAllowedToMerchantException() {
        super(
                400,
                "103",
                "ex.103.bin.not.allowed",
                "Merchant is not Allowed To Use This CARD - Country Restriction (Contact - Alphapay Support)",
                "Country not allowed for this merchant"
        );
    }

    public BINNotAllowedToMerchantException(String message) {
        super(
                400,
                "103",
                "ex.103.bin.not.allowed",
                "Merchant is not Allowed To Use This CARD - Country Restriction (Contact - Alphapay Support)",
                message
        );
    }
}
