package com.alphapay.payEngine.transactionLogging;

import com.alphapay.payEngine.integration.dto.paymentData.BaseFinancialRequest;
import com.alphapay.payEngine.management.data.Application;
import com.alphapay.payEngine.service.bean.BaseRequest;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import com.alphapay.payEngine.transactionLogging.data.NonFinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.NonFinancialTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.util.Date;

@RestControllerAdvice
public class PayEngineRequestLogger extends RequestBodyAdviceAdapter {

    private final Logger logger = LoggerFactory.getLogger(PayEngineRequestLogger.class);
    @Autowired
    NonFinancialTransactionRepository nonFinancialRepo;

    @Autowired
    FinancialTransactionRepository financialRepo;
    @Autowired
    HttpServletRequest request;


    void logNonFinancialTran(Object body) {
        NonFinancialTransaction tran = new NonFinancialTransaction();
        BeanUtils.copyProperties(body, tran);
        request.setAttribute("payEngineRequestId", tran.getRequestId());
        request.setAttribute("payEngineRequestType", "NON-FINANCIAL");
        tran.setTransactionType(body.getClass().getSimpleName());
        tran.setApplicationId((String) request.getAttribute("applicationId"));
        tran.setApplicationApplication((Long) request.getAttribute("applicationApplicationId"));
        if (((BaseRequest) body).getAuditInfo() != null) {
            BeanUtils.copyProperties(((BaseRequest) body).getAuditInfo(), tran);
        }
        logger.debug("IppppSaving incoming request to db {},tan", tran.getIp());
        try {
            nonFinancialRepo.save(tran);
        } catch (Exception ex) {
            if (ex instanceof ConstraintViolationException || ex.getCause() instanceof ConstraintViolationException)
                throw new DuplicateTransactionException();
            else throw ex;
        }

    }

    void logFinancialTran(Object body) {
        FinancialTransaction tran = new FinancialTransaction();
        BeanUtils.copyProperties(body, tran);
        request.setAttribute("payEngineRequestId", tran.getRequestId());
        request.setAttribute("payEngineRequestType", "FINANCIAL");
        tran.setTransactionType(body.getClass().getSimpleName());
        tran.setApplicationId((String) request.getAttribute("applicationId"));
        tran.setApplicationChannel((Long) request.getAttribute("applicationApplicationId"));
        if (((BaseRequest) body).getAuditInfo() != null) {
            BeanUtils.copyProperties(((BaseRequest) body).getAuditInfo(), tran);
        }
//        if (body instanceof BaseFinancialRequest) {
//            tran.setAccountIdentifierEncryptedValue(((BaseFinancialRequest)body).getCustomerIdentifier());
//        }
//        if(body instanceof Transfer) {
//            tran.setToAccountIdentifierEncryptedValue(((Transfer)body).getToAccountIdentifier());
//            if (((Transfer)body).getToAccountType().equals(AccountType.PAN.toString()))
//                tran.setToAccountIdentifier(BeanUtility.maskPan(((Transfer)body).getToAccountIdentifier()));
//        }
        if (body instanceof BaseFinancialRequest) {
            tran.setIncomingPaymentAttributes(((BaseFinancialRequest) body).getPaymentAttributes());
        }
        logger.debug("Saving incoming request to db {},tan", tran.getRequestId());
        try {
            financialRepo.save(tran);
        } catch (Exception ex) {
            if (ex instanceof ConstraintViolationException || ex.getCause() instanceof ConstraintViolationException)
                throw new DuplicateTransactionException();
            else throw ex;
        }

    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        logger.trace("OptimusRequestLogger:afterBodyRead");
        logger.trace(String.valueOf(request.getDispatcherType()));
        if (body instanceof BaseRequest) {
            if (!Boolean.TRUE.equals(((BaseRequest) body).getFiltered())) {
                if (body instanceof BaseFinancialRequest) {
                    logFinancialTran(body);
                } else {
                    logNonFinancialTran(body);
                }
            }
        }

        try {
            Application app = ((Application) request.getAttribute("applicationObject"));
            if (app != null) {
                if (body instanceof BaseRequest) {
                    ((BaseRequest) body).setApplication(app);
                    ((BaseRequest) body).setApplicationId(app.getApplicationId());
                    ((BaseRequest) body).setTransactionDateTime(new Date());
                    ((BaseRequest) body).setFiltered(true);

                }
            }
        } catch (Throwable ex) {
            logger.error("Failed to set channel to request", ex);
        }
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }
}