package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class MerchantDocumentMissingException extends BaseWebApplicationException {
    public MerchantDocumentMissingException() {
        super(
                403,
                "7220",
                "ex.7220.merchant.document.notUploaded",
                "Please Upload Documents for this merchant",
                "Please Upload Documents for this merchant"
        );
    }
}
