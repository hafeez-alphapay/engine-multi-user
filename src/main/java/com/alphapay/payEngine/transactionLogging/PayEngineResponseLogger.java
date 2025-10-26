package com.alphapay.payEngine.transactionLogging;

import com.alphapay.payEngine.account.management.model.AccountType;
import com.alphapay.payEngine.common.bean.ErrorResponse;
import com.alphapay.payEngine.integration.dto.paymentData.ApiResponse;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import com.alphapay.payEngine.transactionLogging.data.NonFinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.NonFinancialTransactionRepository;
import com.alphapay.payEngine.transactions.bean.Transfer;
import com.alphapay.payEngine.utilities.BeanUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class PayEngineResponseLogger implements ResponseBodyAdvice<Object> {

    public static final String TIMEOUT_RESPONSE = "9500";
    public static final String SUCCESS_STATUS = "Success";
    public static final String FAILED_STATUS = "Failed";
    public static final String UNKNOWN_STATUS = "Unknown";
    private final Logger logger = LoggerFactory.getLogger(PayEngineResponseLogger.class);
    @Autowired
    NonFinancialTransactionRepository nonFinancialRepo;
    @Autowired
    FinancialTransactionRepository financialRepo;
    @Autowired
    HttpServletRequest httpServletRequest;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            logger.trace("PayEngineResponseLogger:beforeBodyWrite invoked to log response");
            if (httpServletRequest.getAttribute("payEngineRequestType") != null) {
                String type = (String) httpServletRequest.getAttribute("payEngineRequestType");
                logger.debug("payEngineRequestType is{}", type);
                if (type.equals("FINANCIAL")) {
                    FinancialTransaction transaction = financialRepo.findByRequestId((String) httpServletRequest.getAttribute("payEngineRequestId"));
                    logger.debug("payEngineRequestId::::{}",(String) httpServletRequest.getAttribute("payEngineRequestId"));
                    if (body instanceof ErrorResponse) {
                        if (((ErrorResponse) body).getRequestBody() != null) {
                            Object incomingRequest = ((ErrorResponse) body).getRequestBody();
                            if (incomingRequest instanceof Transfer) {
                                if (((Transfer) incomingRequest).getToAccountType().equals(AccountType.PAN.toString()))
                                    ((Transfer) incomingRequest).setToAccountIdentifier(BeanUtility.maskPan(((Transfer) incomingRequest).getToAccountIdentifier()));
                            }
                            BeanUtility.copyProperties(incomingRequest, transaction);
                        }
                        transaction.setHttpResponseCode(((ErrorResponse) body).getHttpResponseCode() + "");
                        transaction.setResponseMessage(((ErrorResponse) body).getErrorMessage());
                        transaction.setAppResponseCode(((ErrorResponse) body).getErrorCode());
                        transaction.setStatus(FAILED_STATUS);
                        if (TIMEOUT_RESPONSE.equals(transaction.getAppResponseCode())) {
                            transaction.setStatus(UNKNOWN_STATUS);
                        }

                    } else {
                        if (transaction != null) {

                            if (body instanceof ApiResponse) {
                                if (((ApiResponse) body).getResponseData() != null)
                                    transaction.setPaymentResponse(((ApiResponse) body).getResponseData().toMap());
                            }
                            transaction.setHttpResponseCode("200");
                            if (transaction.getResponseMessage() == null)
                                transaction.setResponseMessage("Transaction Completed Successfully");
                            transaction.setAppResponseCode("0");
                            transaction.setStatus(SUCCESS_STATUS);
                        } else {
                        }
                    }
                    if (body != null)
                        BeanUtility.copyProperties(body, transaction);
                    financialRepo.save(transaction);
                    logger.debug("Financial Saving request to db {},status {}",transaction.getRequestId(), transaction.getStatus());
                } else if (type.equals("NON-FINANCIAL")) {
                    NonFinancialTransaction transaction = nonFinancialRepo.findByRequestId((String) httpServletRequest.getAttribute("payEngineRequestId"));
                    if (body instanceof ErrorResponse) {
                        transaction.setHttpResponseCode(((ErrorResponse) body).getHttpResponseCode() + "");
                        transaction.setResponseMessage(((ErrorResponse) body).getErrorMessage());
                        transaction.setAppResponseCode(((ErrorResponse) body).getErrorCode());
                        transaction.setStatus(FAILED_STATUS);

                    } else {
                        transaction.setHttpResponseCode("200");
                        if (transaction.getResponseMessage() == null)
                            transaction.setResponseMessage("Transaction Completed Successfully");
                        transaction.setAppResponseCode("0");
                        transaction.setStatus(SUCCESS_STATUS);
                        logger.debug("transactionResponse::::{}", transaction);

                    }
                    if (body != null)
                        BeanUtility.copyProperties(body, transaction);
                    nonFinancialRepo.save(transaction);
                    logger.debug("NON-Financial Saving request to db {},tan", transaction.getRequestId());
                }
            } else {

            }
        } catch (Throwable ex) {
            logger.error("Unable to save response {}", ex);
            logger.error("Unable to save response {}", ex.getLocalizedMessage());
        }
        return body;
    }
}