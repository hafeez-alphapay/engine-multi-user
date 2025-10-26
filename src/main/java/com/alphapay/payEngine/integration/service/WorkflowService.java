package com.alphapay.payEngine.integration.service;

import com.alphapay.payEngine.alphaServices.model.IntegrationApiToken;

public interface WorkflowService {
    public Object executeWorkflow(String workflowName, Object request, String requestId) ;
    public String issueMbmeToken(IntegrationApiToken token) ;
    }
