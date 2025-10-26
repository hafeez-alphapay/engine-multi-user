package com.alphapay.payEngine.alphaServices.service;

import com.alphapay.payEngine.account.management.dto.request.MerchantServiceRequest;
import com.alphapay.payEngine.alphaServices.dto.request.GetServiceRequest;
import com.alphapay.payEngine.alphaServices.dto.response.MerchantStats;
import com.alphapay.payEngine.alphaServices.dto.response.ServiceResponse;
import com.alphapay.payEngine.alphaServices.model.MerchantAlphaPayServicesEntity;
import com.alphapay.payEngine.alphaServices.model.MerchantServiceConfigEntity;
import com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration.CallBackRequest;
import com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration.GenerateApiKeyRequest;
import com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration.GetGatewayConfigurationRequest;
import com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration.WebhookRequest;
import com.alphapay.payEngine.integration.dto.response.paymentGatewayIntegration.MerchantGatewayConfigurationResponse;

import java.util.List;

public interface MerchantAlphaPayServicesService {
    void addMerchantService(Long merchantId, List<MerchantServiceRequest> merchantServices);
    List<ServiceResponse> getAllServices(GetServiceRequest request);
    List<MerchantAlphaPayServicesEntity> getMerchantServices(Long merchantId);
    List<MerchantAlphaPayServicesEntity> getMerchantActiveServices(Long merchantId);
    MerchantAlphaPayServicesEntity checkMerchantService(Long merchantId, String serviceId);

    MerchantServiceConfigEntity validatedMerchantApiKey(String apiKey );
    MerchantServiceConfigEntity getConfigEntityByMerchantId(Long merchantId );
    MerchantGatewayConfigurationResponse getGatewayConfiguration(GetGatewayConfigurationRequest request);
    MerchantGatewayConfigurationResponse configureCallBack(CallBackRequest request);
    MerchantGatewayConfigurationResponse configureWebhook(WebhookRequest request);
    MerchantGatewayConfigurationResponse generateApiKey(GenerateApiKeyRequest request);
    MerchantStats getLast24hStats();
}