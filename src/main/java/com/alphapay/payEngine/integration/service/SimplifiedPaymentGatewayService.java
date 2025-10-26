package com.alphapay.payEngine.integration.service;

import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentRequest;
import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentResponse;
import com.alphapay.payEngine.integration.dto.paymentData.InitiatePaymentResponse;
import com.alphapay.payEngine.integration.dto.request.DirectPaymentRefundRequest;
import com.alphapay.payEngine.integration.dto.request.GeneratePaymentGatewayInvoiceAndInitiatePaymentRequest;
import com.alphapay.payEngine.integration.dto.request.PaymentStatusRequest;
import com.alphapay.payEngine.integration.dto.request.RefundStatusRequest;
import com.alphapay.payEngine.integration.dto.response.RefundQueryResponse;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import jakarta.validation.Valid;

public interface SimplifiedPaymentGatewayService {

    TransactionStatusResponse getTransactionStatus(@Valid PaymentStatusRequest request);

    TransactionStatusResponse getTransactionStatus(@Valid PaymentStatusRequest request, Boolean bypassClientAPIKeyValidation);


    ExecutePaymentResponse executeDirectPayment(ExecutePaymentRequest request);

    InitiatePaymentResponse initiateDirectPayment(GeneratePaymentGatewayInvoiceAndInitiatePaymentRequest request);

    FinancialTransaction executeRefundDirectPayment(@Valid DirectPaymentRefundRequest request);

    RefundQueryResponse executeRefundStatus(RefundStatusRequest request);

    void executeRefundJob(String paymentId);

}
