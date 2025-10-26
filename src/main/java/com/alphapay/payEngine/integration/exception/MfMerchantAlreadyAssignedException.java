package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class MfMerchantAlreadyAssignedException extends BaseWebApplicationException {

    public MfMerchantAlreadyAssignedException() {
        super(
                409,
                "7509",
                "ex.7509.merchant.registered.myfatoorah",
                "Merchant Already registered on MyFatoorah",
                "Merchant Already registered on MyFatoorah"
        );
    }
}
