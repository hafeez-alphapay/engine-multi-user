package com.alphapay.payEngine.common.exception;

public class EncryptionException extends BaseWebApplicationException {

    public EncryptionException() {
        super(502, "5210", "ex.5201.gateway.encryption.failure", "Payment Gateway Encryption Error", "An error occurred during encryption/decryption process");
    }

    public EncryptionException(String applicationMessage) {
        super(502, "5210", "ex.5201.gateway.encryption.failure", "Payment Gateway Encryption Error", applicationMessage);
    }
}

