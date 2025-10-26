package com.alphapay.payEngine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MyFatoorahConfig {
    @Value("${myfatoorah.apiKey}")
    private String apiKey;

    @Value("${myfatoorah.isTest}")
    private boolean isTest;

    @Value("${myfatoorah.vcCode}")
    private String vcCode;

    @Value("${myfatoorah.api.baseUrl}")
    private String baseUrl;

    @Value("${myfatoorah.call.back.url}")
    private String callBackUrl;

    @Value("${myfatoorah.call.error.url}")
    private String errorUrl;

    @Value("${myfatoorah.webhook.url}")
    private String webhookUrl;

    public String getApiKey() {
        return apiKey;
    }

    public boolean isTest() {
        return isTest;
    }

    public String getVcCode() {
        return vcCode;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    public void setVcCode(String vcCode) {
        this.vcCode = vcCode;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }

    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}
