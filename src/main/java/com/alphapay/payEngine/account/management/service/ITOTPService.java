package com.alphapay.payEngine.account.management.service;

import com.alphapay.payEngine.common.otp.models.TOTPKey;
import com.google.zxing.WriterException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface ITOTPService {
    public String generateQRCode(String text, String provider) throws WriterException, IOException;
    public boolean validateTOTP(String requestId,String userKeyEncrypted, String totpValue) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;
    public TOTPKey generateTOTPKey(String requestId, String provider, String cif) throws WriterException, IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

}