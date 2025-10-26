package com.alphapay.payEngine.service;

import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public interface RestTemlateFactoryService{

    public RestTemplate getRestTemplate(int connectionTimeoutMs) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException;
    public RestTemplate getRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException;
}
