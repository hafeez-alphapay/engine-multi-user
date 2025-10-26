package com.alphapay.payEngine.integration.exception;

import com.alphapay.payEngine.common.exception.BaseWebApplicationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class CustomException extends BaseWebApplicationException {
  public CustomException(String errorMessage, String errorDetails) {
    super(
            400,
            "5203",
            "backend.service.processing.failure",
            formatErrorMessage(errorMessage, errorDetails),
            formatErrorMessage(errorMessage, errorDetails)
    );
  }

  private static String formatErrorMessage(String errorMessage, String errorDetails) {
    try {
      if (errorDetails == null) {
        return "Backend Service Processing Error ";
      }
      ObjectMapper mapper = new ObjectMapper();
      JsonNode errorJson = mapper.readTree(errorDetails);
      StringBuilder sb = new StringBuilder(errorMessage);
      if (errorJson.has("Message")) {
        String message = errorJson.get("Message").asText();
        sb.append(message);
      }
      if (errorJson.has("ValidationErrors")) {
        JsonNode validationErrors = errorJson.get("ValidationErrors");
        for (JsonNode error : validationErrors) {
          sb.append("; ").append(error.get("Name").asText()).append(": ").append(error.get("Error").asText());
        }
      }

      if (errorJson.has("FieldsErrors")) {
        JsonNode validationErrors = errorJson.get("FieldsErrors");
        for (JsonNode error : validationErrors) {
          sb.append("; ").append(error.get("Name").asText()).append(": ").append(error.get("Error").asText());
        }
      }
      if(!errorJson.has("Message") && !errorJson.has("ValidationErrors") && !errorJson.has("FieldsErrors")){
        sb.append(errorDetails);
      }

      return sb.toString();
    } catch (IOException e) {
      return errorMessage + "; Details: " + errorDetails;
    }
  }
}

