package com.alphapay.payEngine.controller.alphaServices;

import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.alphaServices.dto.request.GenerateInvoiceRequest;
import com.alphapay.payEngine.alphaServices.dto.request.GetLinkDetails;
import com.alphapay.payEngine.alphaServices.dto.request.GetMerchantPaymentMethod;
import com.alphapay.payEngine.alphaServices.dto.request.LinkHistoryRequest;
import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkCreationResponse;
import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkResponse;
import com.alphapay.payEngine.alphaServices.service.GenerateLinkService;
import com.alphapay.payEngine.integration.model.PaymentMethodEntity;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/invoice")
@Slf4j
public class InvoiceLinkController {

    @Autowired
    GenerateLinkService payLinkService;

    @RequestMapping(value = "/createInvoice", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public PaymentLinkCreationResponse generateGenericPaymentLink(@RequestBody @Valid GenerateInvoiceRequest payLinkRequest) {
        log.debug("payLinkRequest----------->{}", payLinkRequest);
        return payLinkService.generateInvoiceLink(payLinkRequest);
    }

    @RequestMapping(value = "/getInvoices", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public PaginatedResponse<PaymentLinkResponse> getPaymentLink(@RequestBody @Valid LinkHistoryRequest request) {
        return payLinkService.getPaymentLinks(request);
    }

    @RequestMapping(value = "/invoiceDetails", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public PaymentLinkResponse linkDetails(@RequestBody @Valid GetLinkDetails request) {
        return payLinkService.getLinkDetails(request);
    }

    @RequestMapping(value = "/markExpired", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public PaymentLinkResponse markExpired(@RequestBody @Valid GetLinkDetails request) {
        return payLinkService.markExpired(request);
    }


    @RequestMapping(value = "/merchantPaymentMethods", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public List<PaymentMethodEntity> getMerchantPaymentMethods(@RequestBody @Valid GetMerchantPaymentMethod request) {
        return payLinkService.getPaymentMethod(request);
    }
}
