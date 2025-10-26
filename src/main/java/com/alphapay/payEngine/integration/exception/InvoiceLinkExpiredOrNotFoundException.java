package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class InvoiceLinkExpiredOrNotFoundException extends BaseWebApplicationException {
    public InvoiceLinkExpiredOrNotFoundException() {
        super(
                404,
                "7216",
                "ex.7216.invoice.expired.not.found",
                "This invoice does not exist or is expired.",
                "Invoice Expired or not found"
        );
    }
}
