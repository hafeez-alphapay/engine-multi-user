package com.alphapay.payEngine.controller.alphaServices;

import com.alphapay.payEngine.alphaServices.dto.request.GetServiceRequest;
import com.alphapay.payEngine.alphaServices.dto.response.ServiceResponse;
import com.alphapay.payEngine.alphaServices.service.MerchantAlphaPayServicesService;
import com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration.CallBackRequest;
import com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration.GenerateApiKeyRequest;
import com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration.GetGatewayConfigurationRequest;
import com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration.WebhookRequest;
import com.alphapay.payEngine.integration.dto.response.paymentGatewayIntegration.MerchantGatewayConfigurationResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceManagementController {

    @Autowired
    private MerchantAlphaPayServicesService merchantAlphaPayService;

    @RequestMapping(value = "/all", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public List<ServiceResponse> getAllServices(@Valid @RequestBody GetServiceRequest request) {
        return merchantAlphaPayService.getAllServices(request);
    }


    @PostMapping("/generateApiKey")
    public MerchantGatewayConfigurationResponse generateApiKey(@RequestBody @Valid GenerateApiKeyRequest request) {
        return merchantAlphaPayService.generateApiKey(request);
    }


    @PostMapping("/getGatewayConfiguration")
    public MerchantGatewayConfigurationResponse getGatewayConfiguration(@RequestBody @Valid GetGatewayConfigurationRequest request) {
        return merchantAlphaPayService.getGatewayConfiguration(request);
    }


    @PostMapping("/configureWebhook")
    public MerchantGatewayConfigurationResponse configureWebhook(@RequestBody @Valid WebhookRequest request) {
        return merchantAlphaPayService.configureWebhook(request);
    }


    @PostMapping("/configureCallBack")
    public MerchantGatewayConfigurationResponse configureCallBack(@RequestBody @Valid CallBackRequest request) {
        return merchantAlphaPayService.configureCallBack(request);
    }

}
