package com.alphapay.payEngine.alphaServices.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

import java.math.BigDecimal;

public class TransactionAmountExceededException extends BaseWebApplicationException {

    private static final long serialVersionUID = 353887626725752084L;

    public TransactionAmountExceededException(BigDecimal maxTransactionAmount, String currency) {
        super(
                400,
                "102",
                "ex.102.transaction.amount.exceeded",
                "Transaction amount exceeds the allowed maximum",
                String.format("Transaction amount exceeds the allowed maximum of %s %s for this merchant.", maxTransactionAmount.toPlainString(), currency)
        );
    }
}
