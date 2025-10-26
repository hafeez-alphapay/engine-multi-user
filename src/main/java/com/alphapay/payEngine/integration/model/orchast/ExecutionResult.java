package com.alphapay.payEngine.integration.model.orchast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.ToString;

@ToString
public class ExecutionResult {
    private final boolean success;
    private final JsonNode response;
    private final ObjectNode mergedResponse;
    private final boolean shouldReturnResponse;

    public String getRawRequest() {
        return rawRequest;
    }

    public void setRawRequest(String rawRequest) {
        this.rawRequest = rawRequest;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    private String rawRequest;
    private String rawResponse;

    public ExecutionResult(boolean success, JsonNode response, ObjectNode mergedResponse, boolean shouldReturnResponse,String rawRequest,String rawResponse) {
        this.success = success;
        this.response = response;
        this.mergedResponse = mergedResponse;
        this.shouldReturnResponse = shouldReturnResponse;
        this.rawRequest=rawRequest;
        this.rawResponse=rawResponse;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public JsonNode getResponse() { return response; }
    public ObjectNode getMergedResponse() { return mergedResponse; }
    public boolean shouldReturnResponse() { return shouldReturnResponse; }
}
