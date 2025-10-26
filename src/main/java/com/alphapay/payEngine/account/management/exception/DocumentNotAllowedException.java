package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class DocumentNotAllowedException extends BaseWebApplicationException {

    public DocumentNotAllowedException() {
        super(451, "7212", "ex.7212.doc.not.allowed", "use another doc option ", "this doc is not allowed");
    }

    public DocumentNotAllowedException(String errorCode, String errorMessageKey) {
        super(451, errorCode, errorMessageKey, "this doc is not allowed", null);
    }

}
