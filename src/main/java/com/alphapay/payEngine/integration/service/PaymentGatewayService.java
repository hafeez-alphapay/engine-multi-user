package com.alphapay.payEngine.integration.service;

import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkCreationResponse;
import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentRequest;
import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentResponse;
import com.alphapay.payEngine.integration.dto.paymentData.InitiatePaymentRequest;
import com.alphapay.payEngine.integration.dto.paymentData.InitiatePaymentResponse;
import com.alphapay.payEngine.integration.dto.request.GeneratePaymentGatewayInvoiceRequest;
import com.alphapay.payEngine.integration.dto.request.PaymentStatusRequest;
import jakarta.validation.Valid;

public interface PaymentGatewayService {

    PaymentLinkCreationResponse generatePaymentGateInvoice(@Valid GeneratePaymentGatewayInvoiceRequest payLinkRequest  );

    TransactionStatusResponse getTransactionStatus(@Valid PaymentStatusRequest request);

    InitiatePaymentResponse initiateDirectPayment(@Valid InitiatePaymentRequest request);

    ExecutePaymentResponse executeDirectPayment(ExecutePaymentRequest request);

    TransactionStatusResponse getTransactionStatusByExternalIdOrPaymentId(@Valid PaymentStatusRequest request);

    TransactionStatusResponse getTransactionStatusByExternalIdOrPaymentId(@Valid PaymentStatusRequest request, Boolean bypassClientAPIKeyValidation);

    void updateInvoiceWithPaymentId(InitiatePaymentResponse initiatePaymentResponse);
}
