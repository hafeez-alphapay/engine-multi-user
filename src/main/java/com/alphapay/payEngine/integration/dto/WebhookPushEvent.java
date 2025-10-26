package com.alphapay.payEngine.integration.dto;

import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;

public record WebhookPushEvent(
        TransactionStatusResponse response,
        PaymentLinkEntity paymentLink,
        String additionalWebhookUrl
) {}

