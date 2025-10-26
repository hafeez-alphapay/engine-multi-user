package com.alphapay.payEngine.account.management.exception;

import com.alphapay.payEngine.common.bean.ErrorResponse;
import com.alphapay.payEngine.common.bean.ValidationError;
import com.alphapay.payEngine.common.exception.BaseWebApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@ControllerAdvice
public class ExceptionHandlers {

    protected static Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    private MessageResolverService messageResolverService;

    @ExceptionHandler(BaseWebApplicationException.class)
    public @ResponseBody
    ErrorResponse baseWebAppException(HttpServletResponse servletResponse, BaseWebApplicationException ex) {
        logException(ex);
        servletResponse.setStatus(ex.getStatus());
        ErrorResponse errorResponse = ex.getErrorResponse();
        errorResponse.setErrorMessage(messageResolverService.resolveLocalizedErrorMessage(ex));
        errorResponse.setRequestBody(httpServletRequest.getAttribute("incomingRequestBody"));
        logger.debug("baseWebAppException setErrorMessage:" + errorResponse.getErrorMessage());
        logger.debug("baseWebAppException ex:" + ex);
        errorResponse.setLocalizedErrorMessage(messageResolverService.resolveLocalizedErrorMessage(ex, new Locale("ar")));
        if(ex instanceof PasswordPolicyException)
        {
            logger.debug("Amending policy in response message");
            String policyDesAr=((PasswordPolicyException) ex).getPolicy().getPolicyRuleMessageAr();
            String policyDesEn=((PasswordPolicyException) ex).getPolicy().getPolicyRuleMessageEn();
            errorResponse.setErrorMessage(errorResponse.getErrorMessage()+" "+policyDesEn);
            errorResponse.setLocalizedErrorMessage(errorResponse.getLocalizedErrorMessage()+" "+policyDesAr);
        }
        return errorResponse;
    }

    @ExceptionHandler(ValidationException.class)
    public @ResponseBody
    ErrorResponse baseWebAppValidationException(HttpServletResponse servletResponse, ValidationException ex) {
        logException(ex);
        servletResponse.setStatus(ex.getStatus());
        ErrorResponse response= ex.getErrorResponse();
        response.setRequestBody(httpServletRequest.getAttribute("incomingRequestBody"));
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse ValidationError(HttpServletResponse servletResponse, MethodArgumentNotValidException ex) {
        logException(ex);
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        ApplicationRuntimeException exception = new ApplicationRuntimeException(
                HttpStatus.BAD_REQUEST.value(), "4000", "ex.4000.method.args.invalid", "Validation Error", "The data passed in the request was invalid. Please check and resubmit");
        ErrorResponse response = exception.getErrorResponse();
        response.setErrorMessage(messageResolverService.resolveLocalizedErrorMessage(exception));
        response.setLocalizedErrorMessage(messageResolverService.resolveLocalizedErrorMessage(exception, new Locale("ar")));
        response.setValidationErrors(processFieldErrors(fieldErrors));
        servletResponse.setStatus(exception.getStatus());
        response.setRequestBody(httpServletRequest.getAttribute("incomingRequestBody"));
        return response;
    }

    @ExceptionHandler(RestClientException.class)
    public @ResponseBody
    ErrorResponse restClientException(HttpServletResponse servletResponse, RestClientException ex) {
        logException(ex);
        ApplicationRuntimeException exception = new ApplicationRuntimeException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "5206", "ex.5206.rest.client.error", "Connection Error", "Error connecting with remote service");
        ErrorResponse response = exception.getErrorResponse();
        response.setErrorMessage(messageResolverService.resolveLocalizedErrorMessage(exception));
        response.setLocalizedErrorMessage(messageResolverService.resolveLocalizedErrorMessage(exception, new Locale("ar")));
        servletResponse.setStatus(exception.getStatus());
        response.setRequestBody(httpServletRequest.getAttribute("incomingRequestBody"));
        return response;
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, HttpMediaTypeNotSupportedException.class})
    public @ResponseBody
    ErrorResponse httpMethod(HttpServletResponse servletResponse, Exception ex) {
        logException(ex);
        ApplicationRuntimeException exception = new ApplicationRuntimeException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "5205", "ex.5205.http.request.notSupported", "Request Error", "Http Content-Type or Method Not Supported");
        ErrorResponse response = exception.getErrorResponse();
        response.setErrorMessage(messageResolverService.resolveLocalizedErrorMessage(exception));
        response.setLocalizedErrorMessage(messageResolverService.resolveLocalizedErrorMessage(exception, new Locale("ar")));
        servletResponse.setStatus(exception.getStatus());
        response.setRequestBody(httpServletRequest.getAttribute("incomingRequestBody"));
        return response;

    }

    @ExceptionHandler(Throwable.class)
    public @ResponseBody
    ErrorResponse otherThrowable(HttpServletResponse servletResponse, Throwable e) {
        logException(e);
        ApplicationRuntimeException exception = new ApplicationRuntimeException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "5000", "ex.5000.default.system.error", "System Error", "System Error");
        ErrorResponse response = exception.getErrorResponse();
        response.setErrorMessage(messageResolverService.resolveLocalizedErrorMessage(exception));
        response.setLocalizedErrorMessage(messageResolverService.resolveLocalizedErrorMessage(exception, new Locale("ar")));
        response.setRequestBody(httpServletRequest.getAttribute("incomingRequestBody"));
        servletResponse.setStatus(exception.getStatus());
        return response;

    }

    private List<ValidationError> processFieldErrors(List<FieldError> fieldErrors) {
        List<ValidationError> errors = new ArrayList<ValidationError>();
        for (FieldError fieldError : fieldErrors) {
            String localizedErrorMessage = messageResolverService.resolveLocalizedErrorMessage(fieldError);
            ValidationError error = new ValidationError();
            error.setMessage(localizedErrorMessage);
            error.setPropertyName(fieldError.getField());
            error.setPropertyValue(fieldError.getRejectedValue() != null ? fieldError.getRejectedValue().toString() : null);
            errors.add(error);
        }
        return errors;
    }

    private void logException(Throwable ex) {
        logger.error(ex.getLocalizedMessage(), ex);
    }


}
