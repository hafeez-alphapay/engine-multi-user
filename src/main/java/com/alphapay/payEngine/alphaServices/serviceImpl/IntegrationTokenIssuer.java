package com.alphapay.payEngine.alphaServices.serviceImpl;

import com.alphapay.payEngine.alphaServices.model.IntegrationApiToken;
import com.alphapay.payEngine.alphaServices.service.TokenIssuer;
import com.alphapay.payEngine.common.encryption.EncryptionService;
import com.alphapay.payEngine.integration.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class IntegrationTokenIssuer implements TokenIssuer {

    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private WorkflowService workflowOrchestratorService;

    @Override
    public String issue(IntegrationApiToken token) {
        log.debug("Issuing new Token for {}",token);
      return workflowOrchestratorService.issueMbmeToken(token);
    }

    @Override
    public Duration renewWindow() {
        return TokenIssuer.super.renewWindow();
    }
}
