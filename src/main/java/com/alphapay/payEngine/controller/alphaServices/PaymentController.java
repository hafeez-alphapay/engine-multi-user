package com.alphapay.payEngine.controller.alphaServices;

import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.common.bean.VerifyResult;
import com.alphapay.payEngine.integration.dto.paymentData.*;
import com.alphapay.payEngine.integration.dto.request.*;
import com.alphapay.payEngine.integration.dto.response.ChargesResult;
import com.alphapay.payEngine.integration.dto.response.InvoiceSummaryResponse;
import com.alphapay.payEngine.integration.service.ChargesCalculatorService;
import com.alphapay.payEngine.integration.service.ExecutePaymentService;
import com.alphapay.payEngine.integration.service.InitiatePaymentService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {
    private static final String SECRET_KEY = "6763b847592e24d72823ce44"; // ⚠ same key


    @Autowired
    private InitiatePaymentService initiatePaymentService;

    @Autowired
    private ExecutePaymentService executePaymentService;

    @Autowired
    private ChargesCalculatorService chargesCalculatorService;

    @PostMapping("/invoice-summary")
    public InvoiceSummaryResponse fetchInvoiceSummary(@RequestBody @Valid InvoiceSummaryRequest request) {
        return initiatePaymentService.invoiceSummary(request);
    }


    @PostMapping("/initiate-payment")
    public ResponseEntity<InitiatePaymentResponse> initiateInvoicePayment(@RequestBody @Valid InitiatePaymentRequest request) {
        InitiatePaymentResponse response = initiatePaymentService.initiateInvoicePayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/execute-payment")
    public ExecutePaymentResponse executePayment(@RequestBody ExecutePaymentRequest request) throws Exception {
        return executePaymentService.executePayment(request);
    }

    @PostMapping("/processSuccess")
    public TransactionStatusResponse processSuccess(@RequestBody PaymentStatusRequest request) {
        return initiatePaymentService.processStatus(request);
    }


    @PostMapping("/v2/status")
    public TransactionStatusResponse processSuccessV2(@RequestBody PaymentStatusRequest request) {
        return initiatePaymentService.processStatus(request,Boolean.TRUE);
    }

    @PostMapping("/validateMBME")
    public ResponseEntity<VerifyResult> verify(@RequestBody ValidateMBMERequest body) {
        log.debug("Incoming JSON  : {}", body);
        return ResponseEntity.ok(initiatePaymentService.processSignatureVerification(body));
    }


    @PostMapping("/processError")
    public TransactionStatusResponse processError(@RequestBody PaymentStatusRequest request) {
        return initiatePaymentService.processStatus(request);
    }


    @PostMapping("/makeRefund")
    public FinancialTransaction makeRefund(@RequestBody PortalRefundRequest request) {
        return initiatePaymentService.makeRefund(request);
    }

    @PostMapping("/initiate-session")
    public ResponseEntity<InitiateSessionResponse> initiateSessionPayment(@RequestBody @Valid InitiateSessionRequest request) {
        InitiateSessionResponse response = initiatePaymentService.initiateSession(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chargesCalculator")
    public ResponseEntity<ChargesResult> chargesCalculatorService(@RequestBody @Valid CalculateChargesRequest request) {
        ChargesResult response = chargesCalculatorService.calculateCharges(null,request.getPaymentId());
        return ResponseEntity.ok(response);
    }
}
