package com.alphapay.payEngine.controller.mbme;

import com.alphapay.payEngine.integration.dto.request.CreateSupplier;
import com.alphapay.payEngine.integration.dto.request.CustomizeSupplierCommissions;
import com.alphapay.payEngine.integration.dto.request.MbmeUserLogin;
import com.alphapay.payEngine.integration.service.MBMEIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class MBMEController {
    @Autowired
    private MBMEIntegrationService mbmeIntegrationService;

    @PostMapping("/mbme/login")
    public Object loginMbme(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.authLogin(request);
    }

    @PostMapping("/mbme/getKycDocumentList")
    public Object getKycDocumentList(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.getKycDocumentList(request);
    }

    @PostMapping("/mbme/register-merchant")
    public Object registerMerchant(@RequestBody CreateSupplier request) throws Exception {
        return mbmeIntegrationService.registerMerchant(request);
    }

    @PostMapping("/mbme/uploadMerchantDocument")
    public Object uploadMerchantDocument(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.uploadMerchantDocument(request);
    }


    @PostMapping("/mbme/submit-merchant-kyc")
    public Object submitMerchantKyc(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.submitKyc(request);
    }
    @PostMapping("/mbme/get_kyc_status")
    public Object getMerchantKycStatus(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.getKycStatus(request);
    }

    @PostMapping("/mbme/customizeSupplierCommissions")
    public CreateSupplier customizeSupplierCommissions(@RequestBody CustomizeSupplierCommissions request) throws Exception {
        return mbmeIntegrationService.customizeSupplierCommissions(request);
    }


    @PostMapping("/mbme/update_webhook_info")
    public Object updateWebhookInfo(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.updateWebhookInfo(request);
    }


    @PostMapping("/mbme/get_webhook_details")
    public Object getWebhookInfo(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.getWebhookInfo(request);
    }


    @PostMapping("/mbme/generate_new_api_access_key")
    public Object generateNewApiAccessKey(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.generateNewApiAccessKey(request);
    }


    @PostMapping("/mbme/get_merchant_api_access_key_info")
    public Object getMerchantApiAccessKey(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.getMerchantApiAccessKey(request);
    }


    @PostMapping("/mbme/get_merchant_payment_key")
    public Object getMerchantPaymentKey(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.getMerchantPaymentKey(request);
    }
    @PostMapping("/mbme/generate_merchant_payment_key")
    public Object generateMerchantPaymentKey(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.generateMerchantPaymentKey(request);
    }
    @PostMapping("/mbme/update_callback_url")
    public Object updateCallbackUrl(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.updateCallbackUrl(request);
    }

    @PostMapping("/mbme/get_callback_url")
    public Object getCallbackUrl(@RequestBody MbmeUserLogin request) throws Exception {
        return mbmeIntegrationService.getCallbackUrl(request);
    }


}