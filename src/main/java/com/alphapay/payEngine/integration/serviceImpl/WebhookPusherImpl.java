package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.alphaServices.model.MerchantServiceConfigEntity;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.repository.MerchantServiceConfigRepository;
import com.alphapay.payEngine.integration.dto.WebhookPushEvent;
import com.alphapay.payEngine.integration.model.WebhookLogEntity;
import com.alphapay.payEngine.integration.repository.WebhookLogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class WebhookPusherImpl {

    private final WebhookSender webhookSender;
    public WebhookPusherImpl(WebhookSender webhookSender) {
        this.webhookSender = webhookSender;
    }
    //@Async("webhookExecutor")
    //@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("webhookExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true) // uncomment temp if needed

    public void onCommit(WebhookPushEvent evt) {
        webhookSender.send(evt.response(), evt.paymentLink(), evt.additionalWebhookUrl());
    }
}
