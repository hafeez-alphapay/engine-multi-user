package com.alphapay.payEngine.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.alphapay.payEngine.common.bean.ValidationError;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ErrorResponse {

    private String errorCode;
    private String errorMessage;
    private String localizedErrorMessage;
    private String applicationMessage;
    @JsonIgnore
    private Object requestBody;
    @JsonIgnore
    private int httpResponseCode;

    private List<ValidationError> validationErrors = new ArrayList<ValidationError>();

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public void setHttpResponseCode(int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    @JsonProperty("errorMessageEn")
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String consumerMessage) {
        this.errorMessage = consumerMessage;
    }
    @JsonProperty("errorMessageAr")
    public String getLocalizedErrorMessage() {
        return localizedErrorMessage;
    }

    public void setLocalizedErrorMessage(String localizedErrorMessage) {
        this.localizedErrorMessage = localizedErrorMessage;
    }

}
