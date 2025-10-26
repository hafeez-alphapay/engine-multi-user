package com.alphapay.payEngine.financial.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;
import org.springframework.http.HttpStatus;

public class MerchantWalletStatusException extends BaseWebApplicationException {

    public MerchantWalletStatusException(Long merchantId, String currency) {
        super(HttpStatus.CONFLICT.value(),
                "WALLET-409",
                "error.wallet.status.inactive",
                null,
                null,
                new Object[]{merchantId, currency});
    }
}
