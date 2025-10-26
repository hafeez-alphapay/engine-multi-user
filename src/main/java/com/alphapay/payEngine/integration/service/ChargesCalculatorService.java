package com.alphapay.payEngine.integration.service;

import com.alphapay.payEngine.integration.dto.response.ChargesResult;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;

public interface ChargesCalculatorService {
    ChargesResult calculateCharges(FinancialTransaction transaction, String paymentId);
}