package com.alphapay.payEngine.integration.service;

import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.common.bean.VerifyResult;
import com.alphapay.payEngine.integration.dto.paymentData.*;
import com.alphapay.payEngine.integration.dto.request.*;
import com.alphapay.payEngine.integration.dto.response.InvoiceSummaryResponse;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import jakarta.validation.Valid;

public interface InitiatePaymentService {

    InvoiceSummaryResponse invoiceSummary(@Valid InvoiceSummaryRequest request);

    InitiatePaymentResponse initiateInvoicePayment(InitiatePaymentRequest request);

    TransactionStatusResponse processStatus(PaymentStatusRequest paymentStatusRequest);

     TransactionStatusResponse processStatus(PaymentStatusRequest request,boolean searchByBothPaymentIdAndExternalId);

    FinancialTransaction processRefund(RefundRequest request);

    FinancialTransaction initiateRefundForApproval(RefundRequest request);

    FinancialTransaction makeRefund(PortalRefundRequest request);

    void pushCredit(PaymentLinkEntity paymentLink, FinancialTransaction financialTransaction);

    InitiateSessionResponse initiateSession(@Valid InitiateSessionRequest request);

    VerifyResult processSignatureVerification(ValidateMBMERequest validateMBMERequest);

    FinancialTransaction processMakeRefundUpdated(RefundRequest request, Long providerId);
}
