package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.bean.ErrorResponse;
import com.alphapay.payEngine.common.bean.ValidationError;
import jakarta.validation.ConstraintViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ValidationException extends RuntimeException {

    private final int status = 422;
    private final String errorCode = "4200";
    private final String defaultErrorMessageKey = "ex.4200.validation.error";
    private final String errorMessageKey;
    private String errorMessage;
    private String applicationMessage;
    private List<ValidationError> errors = new ArrayList<ValidationError>();

    public ValidationException() {
        super("Validation Error");
        errorMessageKey=defaultErrorMessageKey;
        errorMessage = "Validation Error";
        applicationMessage = "The data passed in the request was invalid. Please check and resubmit";
    }

    public ValidationException(String message) {
        super(message);
        errorMessageKey = defaultErrorMessageKey;
        errorMessage = message;
    }

    public ValidationException(String errorMessageKey, String errorMessage, String applicationMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.errorMessageKey = errorMessageKey;
        this.applicationMessage = applicationMessage;
    }

    public ValidationException(List<ValidationError> violations) {
        this();
        this.errors = violations;
    }
    
    public ValidationException(Set<? extends ConstraintViolation<?>> violations) {
        this();
        for(ConstraintViolation<?> constraintViolation : violations) {
            ValidationError error = new ValidationError();
            error.setMessage(constraintViolation.getMessage());
            error.setPropertyName(constraintViolation.getPropertyPath().toString());
            error.setPropertyValue(constraintViolation.getInvalidValue() != null ? constraintViolation.getInvalidValue().toString() : null);
            errors.add(error);
        }
    }
    public ErrorResponse getErrorResponse() {
        ErrorResponse response = new ErrorResponse();
        response.setApplicationMessage(applicationMessage);
        response.setErrorMessage(errorMessage);
        response.setLocalizedErrorMessage(errorMessage);
        response.setErrorCode(errorCode);
        response.setValidationErrors(errors);
        return response;
    }

	public int getStatus() {
		return status;
	}
    
    

}
