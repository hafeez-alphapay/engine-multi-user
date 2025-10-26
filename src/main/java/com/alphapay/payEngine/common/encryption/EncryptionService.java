package com.alphapay.payEngine.common.encryption;

import com.alphapay.payEngine.common.exception.EncryptionException;
import com.alphapay.payEngine.model.response.EncryptResponse;
import com.alphapay.payEngine.model.response.EncryptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
@Slf4j
@Service
public class EncryptionService {

    @Autowired
    private Cipher cipher_old;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    GatewayKeysRepository gatewayKeysRepository;

    public EncryptResponse encrypt(String text) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            byte[] encryptedBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return new EncryptResponse(new String(org.apache.commons.codec.binary.Base64.encodeBase64(encryptedBytes), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new EncryptionException("Error encrypting the field");
        }
    }

    public String decrypt(String text) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedBytes = cipher.doFinal(org.apache.commons.codec.binary.Base64.decodeBase64(text.getBytes(StandardCharsets.UTF_8)));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new EncryptionException("Error decrypting the field");
        }
    }

    public void generateAndSaveKeys() throws NoSuchAlgorithmException {
        KeyPair keyPair = KeyUtils.generateKeyPair();
        GatewayKeysEntity keys = gatewayKeysRepository.findByKeyType(KeyType.ENCRYPTION);

        if (keys == null) {
            keys = new GatewayKeysEntity();
            keys.setKeyType(KeyType.ENCRYPTION);
        }
        keys.setPrivateKey(new String(Base64.encode(keyPair.getPrivate().getEncoded())));
        keys.setPublicKey(new String(Base64.encode(keyPair.getPublic().getEncoded())));
        gatewayKeysRepository.save(keys);
    }

    public EncryptionResponse getPublicKey() {
        return new EncryptionResponse(gatewayKeysRepository.findByKeyType(KeyType.ENCRYPTION).getPublicKey());
    }

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH = 128;
    private static final String SECRET = "0123456789abcdef0123456789abcdef"; // 32-char (256-bit) key

    private static SecretKey getKey() {
        return new SecretKeySpec(SECRET.getBytes(), AES);
    }

    public String encryptPass(String plainText) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(TAG_LENGTH, iv));

        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        byte[] encryptedIvAndText = new byte[IV_SIZE + encrypted.length];

        System.arraycopy(iv, 0, encryptedIvAndText, 0, IV_SIZE);
        System.arraycopy(encrypted, 0, encryptedIvAndText, IV_SIZE, encrypted.length);

        return org.apache.commons.codec.binary.Base64.encodeBase64String(encryptedIvAndText);
    }

    public String decryptPass(String encryptedIvText) throws Exception {
        byte[] ivTextBytes = org.apache.commons.codec.binary.Base64.decodeBase64(encryptedIvText);
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(ivTextBytes, 0, iv, 0, IV_SIZE);

        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(TAG_LENGTH, iv));

        byte[] decrypted = cipher.doFinal(ivTextBytes, IV_SIZE, ivTextBytes.length - IV_SIZE);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
