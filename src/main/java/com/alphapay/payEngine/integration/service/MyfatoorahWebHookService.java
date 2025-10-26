package com.alphapay.payEngine.integration.service;

import com.alphapay.payEngine.integration.dto.request.MyFatoorahaWebhookRequest;

public interface MyfatoorahWebHookService {
    void processWebHookResponse(MyFatoorahaWebhookRequest request) throws Exception;
}
