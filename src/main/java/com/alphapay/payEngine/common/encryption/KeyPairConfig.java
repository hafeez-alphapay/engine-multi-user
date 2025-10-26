package com.alphapay.payEngine.common.encryption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@Configuration
public class KeyPairConfig {

    @Autowired
    private GatewayKeysRepository gatewayKeysRepository;


    @Bean
    @Profile("!build") // Only initialize when not in the "build" profile
    public KeyPair keyPair() throws NoSuchAlgorithmException {
        GatewayKeysEntity gatewayKeysEntity = gatewayKeysRepository.findByKeyType(KeyType.ENCRYPTION);

        if (gatewayKeysEntity == null)
            throw new RuntimeException("Key pair not found");

        String publicKey = gatewayKeysEntity.getPublicKey();
        String privateKey = gatewayKeysEntity.getPrivateKey();

        return new KeyPair(KeyUtils.getPublicKey(publicKey), KeyUtils.getPrivateKey(privateKey));
    }

    @Bean
    @Profile("build") // Mock KeyPair for the "build" profile
    public KeyPair mockKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Use a reasonable key size for testing
        return keyPairGenerator.generateKeyPair();
    }

    @Bean
    public KeyPairGenerator keyPairGenerator() throws NoSuchAlgorithmException {
        return KeyPairGenerator.getInstance("RSA");
    }

    @Bean
    public Cipher cipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance("RSA/ECB/PKCS1Padding");
    }
}
