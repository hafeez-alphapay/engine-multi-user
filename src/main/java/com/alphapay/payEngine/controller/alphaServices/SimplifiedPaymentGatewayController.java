package com.alphapay.payEngine.controller.alphaServices;

import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentRequest;
import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentResponse;
import com.alphapay.payEngine.integration.dto.paymentData.InitiatePaymentResponse;
import com.alphapay.payEngine.integration.dto.request.DirectPaymentRefundRequest;
import com.alphapay.payEngine.integration.dto.request.GeneratePaymentGatewayInvoiceAndInitiatePaymentRequest;
import com.alphapay.payEngine.integration.dto.request.PaymentStatusRequest;
import com.alphapay.payEngine.integration.dto.request.RefundStatusRequest;
import com.alphapay.payEngine.integration.dto.response.RefundQueryResponse;
import com.alphapay.payEngine.integration.service.SimplifiedPaymentGatewayService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/paymentGateway/v2")
@Slf4j
public class SimplifiedPaymentGatewayController {

    @Autowired
    private SimplifiedPaymentGatewayService paymentGatewayService;


    @RequestMapping(value = "/initiateDirectPayment", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public InitiatePaymentResponse initiatePayment(@RequestBody @Valid GeneratePaymentGatewayInvoiceAndInitiatePaymentRequest request) {
        return paymentGatewayService.initiateDirectPayment(request);
    }

    @RequestMapping(value = "/executeDirectPayment", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ExecutePaymentResponse executeDirectPayment(@RequestBody @Valid ExecutePaymentRequest request) {
        return paymentGatewayService.executeDirectPayment(request);
    }

    @RequestMapping(value = "/status", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public TransactionStatusResponse getPaymentStatus(@RequestBody @Valid PaymentStatusRequest request) {
        log.trace("Payment status request received: {}", request);
        return paymentGatewayService.getTransactionStatus(request);
    }

    /*
    This for invoice used to create aggregator payments
     */

    @RequestMapping(value = "/paymentInvoiceStatus", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public TransactionStatusResponse getPaymentInvoiceStatus(@RequestBody @Valid PaymentStatusRequest request) {
        return paymentGatewayService.getTransactionStatus(request,Boolean.TRUE);
    }

    @RequestMapping(value = "/refundDirectPayment", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public FinancialTransaction executeRefundDirectPayment(@RequestBody @Valid DirectPaymentRefundRequest request) {
        return paymentGatewayService.executeRefundDirectPayment(request);
    }

    @RequestMapping(value = "/refundStatus", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public RefundQueryResponse executeRefundStatus(@RequestBody @Valid RefundStatusRequest request) {
        return paymentGatewayService.executeRefundStatus(request);
    }
}
