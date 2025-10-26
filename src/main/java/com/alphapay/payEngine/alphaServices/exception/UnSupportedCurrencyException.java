package com.alphapay.payEngine.alphaServices.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class UnSupportedCurrencyException extends BaseWebApplicationException {

    private static final long serialVersionUID = 353887626725752084L;

    public UnSupportedCurrencyException() {
        super(404, "101", "ex.currency.invalid", "Currency is invalid or not supported", "Currency is invalid or not supported");
    }
}
