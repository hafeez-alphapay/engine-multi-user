package com.alphapay.payEngine.integration.service;

import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentRequest;
import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentResponse;

public interface ExecutePaymentService {
    ExecutePaymentResponse executePayment(ExecutePaymentRequest request);
}
