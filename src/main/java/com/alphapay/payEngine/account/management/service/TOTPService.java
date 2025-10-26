package com.alphapay.payEngine.account.management.service;

import com.alphapay.payEngine.common.otp.models.TOTPKey;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TOTPService implements ITOTPService{

    /**
     * Generates a QR code image as a Base64 encoded string.
     *
     * @param text The text to encode into the QR code
     * @return A Base64 encoded string of the QR code image
     */
    @Override
    public String generateQRCode(String text, String provider) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200, hints);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", os);
                return Base64.getEncoder().encodeToString(os.toByteArray());
            }
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for provider {}: {}", provider, e.getMessage());
            return null;
        }
    }


    @Override
    public boolean validateTOTP(String requestId, String encryptedUserKey, String totpValue) {
        try {
            //String decryptedKey = decrypt(encryptedUserKey);
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            int verificationCode = Integer.parseInt(totpValue);
            return gAuth.authorize(encryptedUserKey, verificationCode);
        } catch (Exception e) {
            log.error("Failed to validate TOTP for request ID {}", requestId, e);
            return false;
        }
    }

    @Override
    public TOTPKey generateTOTPKey(String requestId, String provider, String merchantId) {
        try {
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            String secretKey = gAuth.createCredentials().getKey();
            //String encryptedKey = encrypt(secretKey);
            String otpAuthTotpURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    provider, "ALPHA-MID:"+merchantId, secretKey, provider);
            String qrCode = generateQRCode(otpAuthTotpURL,provider);
            return new TOTPKey(secretKey, qrCode, provider, provider);
        } catch (Exception e) {
            log.error("Failed to generate TOTP key for request ID {}", requestId, e);
            return null;
        }
    }
}
