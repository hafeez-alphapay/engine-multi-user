package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentProcessorException extends BaseWebApplicationException {
    public PaymentProcessorException(JsonNode errorMessage, Object[] validationErrors) {
        super(
                400,
                "5203",
                "backend.service.processing.failure",
                errorMessage.get("errorMessageAr").asText(),
                errorMessage.get("errorMessageEn").asText(),
                validationErrors
        );
    }

}

