package com.alphapay.payEngine.integration.service;

import com.alphapay.payEngine.integration.dto.request.CreateSupplier;
import com.alphapay.payEngine.integration.dto.request.CustomizeSupplierCommissions;
import com.alphapay.payEngine.integration.dto.request.MbmeUserLogin;
import com.alphapay.payEngine.integration.dto.response.MbmeUserLoginResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface MBMEIntegrationService {
    Object authLogin(MbmeUserLogin request) throws Exception;

    Object registerMerchant(CreateSupplier request) throws JsonProcessingException, Exception;

    Object getKycDocumentList(MbmeUserLogin request) throws Exception;

    Object submitKyc(MbmeUserLogin request) throws Exception;

    Object getKycStatus(MbmeUserLogin request) throws Exception;

    Object uploadMerchantDocument(MbmeUserLogin request) throws Exception;

    CreateSupplier customizeSupplierCommissions(CustomizeSupplierCommissions request);

    MbmeUserLoginResponse updateWebhookInfo(MbmeUserLogin request) throws Exception;

    MbmeUserLoginResponse getWebhookInfo(MbmeUserLogin request) throws Exception;

    MbmeUserLoginResponse generateNewApiAccessKey(MbmeUserLogin request) throws Exception;

    MbmeUserLoginResponse getMerchantApiAccessKey(MbmeUserLogin request) throws Exception;

    MbmeUserLoginResponse getMerchantPaymentKey(MbmeUserLogin request) throws Exception;

    MbmeUserLoginResponse generateMerchantPaymentKey(MbmeUserLogin request) throws Exception;

    MbmeUserLoginResponse updateCallbackUrl(MbmeUserLogin request) throws Exception;

    MbmeUserLoginResponse getCallbackUrl(MbmeUserLogin request) throws Exception;
}
