package com.alphapay.payEngine.common.encryption;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyUtils {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    public static PublicKey getPublicKey(String key) {
        try {
            byte[] byteKey = org.apache.commons.codec.binary.Base64.decodeBase64(key.getBytes());
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(X509publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to get public key", e);
        }
    }

    public static PrivateKey getPrivateKey(String key) {
        try {
            byte[] byteKey = org.apache.commons.codec.binary.Base64.decodeBase64(key.getBytes());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to get private key", e);
        }
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE, secureRandom);
        return keyPairGenerator.generateKeyPair();
    }
}



