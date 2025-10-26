package com.alphapay.payEngine.controller.alphaServices;

import com.alphapay.payEngine.account.management.dto.response.InvoiceTypeSummaryResponse;
import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.alphaServices.dto.request.GenerateInvoiceRequest;
import com.alphapay.payEngine.alphaServices.dto.request.GetLinkDetails;
import com.alphapay.payEngine.alphaServices.dto.request.LinkHistoryRequest;
import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkCreationResponse;
import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkResponse;
import com.alphapay.payEngine.alphaServices.service.GenerateLinkService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/links/generate")
public class PaymentLinkController {

    @Autowired
    GenerateLinkService payLinkService;

    @RequestMapping(value = "/paymentLink", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public PaymentLinkCreationResponse generateGenericPaymentLink(@RequestBody @Valid GenerateInvoiceRequest payLinkRequest) {
        String prefix = "PAYLINK";
        String type = "STANDARD";
        return payLinkService.generateGenericPaymentLink(payLinkRequest, prefix, type, null,null,null);
    }

    @RequestMapping(value = "/getPaymentLinks", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public PaginatedResponse<PaymentLinkResponse> getPaymentLink(@RequestBody @Valid LinkHistoryRequest request) {
        return payLinkService.getPaymentLinks(request);
    }

    @RequestMapping(value = "/getLinksStatistics", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public List<InvoiceTypeSummaryResponse> getLinksStatistics(@RequestBody @Valid LinkHistoryRequest request) {
        return payLinkService.getPaymentLinkStats(request);
    }

    @RequestMapping(value = "/linkDetails", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public PaymentLinkResponse linkDetails(@RequestBody @Valid GetLinkDetails request) {
        return payLinkService.getLinkDetails(request);
    }
}
