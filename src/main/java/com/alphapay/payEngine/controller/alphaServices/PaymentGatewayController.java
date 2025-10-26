package com.alphapay.payEngine.controller.alphaServices;

import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkCreationResponse;
import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.alphaServices.service.GenerateLinkService;
import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentRequest;
import com.alphapay.payEngine.integration.dto.paymentData.ExecutePaymentResponse;
import com.alphapay.payEngine.integration.dto.paymentData.InitiatePaymentRequest;
import com.alphapay.payEngine.integration.dto.paymentData.InitiatePaymentResponse;
import com.alphapay.payEngine.integration.dto.request.GeneratePaymentGatewayInvoiceRequest;
import com.alphapay.payEngine.integration.dto.request.PaymentStatusRequest;
import com.alphapay.payEngine.integration.service.ExecutePaymentService;
import com.alphapay.payEngine.integration.service.PaymentGatewayService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/paymentGateway")
@Slf4j
public class PaymentGatewayController {

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private ExecutePaymentService executePaymentService;

    @Autowired
    private GenerateLinkService payLinkService;

    @RequestMapping(value = "/createInvoice", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public PaymentLinkCreationResponse generateGenericPaymentLink(@RequestBody @Valid GeneratePaymentGatewayInvoiceRequest request) {
        return paymentGatewayService.generatePaymentGateInvoice(request);
    }

    @RequestMapping(value = "/initiateDirectPayment", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public InitiatePaymentResponse initiatePayment(@RequestBody @Valid InitiatePaymentRequest request) {
        return paymentGatewayService.initiateDirectPayment(request);
    }

    @RequestMapping(value = "/executeDirectPayment", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ExecutePaymentResponse executeDirectPayment(@RequestBody @Valid ExecutePaymentRequest request) {
        return paymentGatewayService.executeDirectPayment(request);
    }

    @RequestMapping(value = "/payment/status", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public TransactionStatusResponse getPaymentStatus(@RequestBody @Valid PaymentStatusRequest request) {
        return paymentGatewayService.getTransactionStatus(request);
    }
}
