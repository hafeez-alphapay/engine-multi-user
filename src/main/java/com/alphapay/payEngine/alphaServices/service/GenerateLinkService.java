package com.alphapay.payEngine.alphaServices.service;

import com.alphapay.payEngine.account.management.dto.response.InvoiceTypeSummaryResponse;
import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.alphaServices.dto.request.GenerateInvoiceRequest;
import com.alphapay.payEngine.alphaServices.dto.request.GetLinkDetails;
import com.alphapay.payEngine.alphaServices.dto.request.GetMerchantPaymentMethod;
import com.alphapay.payEngine.alphaServices.dto.request.LinkHistoryRequest;
import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkCreationResponse;
import com.alphapay.payEngine.alphaServices.dto.response.PaymentLinkResponse;
import com.alphapay.payEngine.integration.model.PaymentMethodEntity;
import jakarta.validation.Valid;

import java.util.List;

public interface GenerateLinkService {
    PaymentLinkCreationResponse generatePaymentGatewayInvoice(GenerateInvoiceRequest request, String callbackUrl, String webhookUrl, String webhookSecretKey, String serviceNameEn);

     PaymentLinkCreationResponse generateGenericPaymentLink(GenerateInvoiceRequest request, String prefix, String type, String callbackUrl, String webhookUrl, String webhookSecretKey);

    PaginatedResponse<PaymentLinkResponse> getPaymentLinks(LinkHistoryRequest request);

    PaymentLinkResponse getLinkDetails(@Valid GetLinkDetails request);

    PaymentLinkResponse markExpired(@Valid GetLinkDetails request);

    PaymentLinkCreationResponse generateInvoiceLink(@Valid GenerateInvoiceRequest payLinkRequest);

    List<PaymentMethodEntity> getPaymentMethod(GetMerchantPaymentMethod request);

    List<InvoiceTypeSummaryResponse> getPaymentLinkStats(LinkHistoryRequest request);
}
