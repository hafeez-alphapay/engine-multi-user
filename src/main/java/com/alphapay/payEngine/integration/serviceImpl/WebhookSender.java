package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.account.merchantKyc.repository.MerchantRepository;
import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.alphaServices.model.MerchantServiceConfigEntity;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.repository.MerchantServiceConfigRepository;
import com.alphapay.payEngine.integration.model.WebhookLogEntity;
import com.alphapay.payEngine.integration.repository.WebhookLogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class WebhookSender {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<Integer> SUCCESS_CODES = List.of(200, 201);


    @Autowired
    private WebhookLogRepository webhookLogRepository;

    @Autowired
    MerchantRepository merchantRepository;

    @Autowired
    private MerchantServiceConfigRepository merchantServiceConfigRepository;

    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void send(TransactionStatusResponse response, PaymentLinkEntity paymentLink, String additionalWebhookUrl) {
        {

            log.debug("***************");
            List<String> signedAttributes=null;
            List<String> signedAttributesDefault = Arrays.asList(
                    "responseData.invoiceId",
                    "responseData.invoiceStatus",
                    "responseData.invoiceReference"
            );
            List<String> signedAttributesRefund = Arrays.asList(
                    "refundData.refundStatus",
                    "refundData.paymentId",
                    "refundData.refundId"
            );

            if(response.getRequestType().equals("Update_Refund_Status"))
                signedAttributes=signedAttributesRefund;
            else
                signedAttributes=signedAttributesDefault;

            String flatStatusAndIDTAG = buildSelectiveSignatureString(response, signedAttributes);

            long count = webhookLogRepository.countByFlatPayloadPriorSignAndResponseCodeIn(
                    flatStatusAndIDTAG, SUCCESS_CODES);
            if(count>0) {
                log.info("Webhook already sent successfully for paymentId: {} and invoiceId: {}. Skipping duplicate push.",
                        paymentLink.getPaymentId(), paymentLink.getInvoiceId());
                return;
            }

            String url = additionalWebhookUrl;
            if(url==null || url.isEmpty() || url.isBlank())
                url= paymentLink.getWebhookUrl();
            String secretKey = paymentLink.getWebhookSecretKey();
            if (secretKey == null || secretKey.isBlank() || secretKey.isEmpty() || "null".equals(secretKey)||url==null || url.isEmpty()|| url.isBlank()) {
                log.warn("Webhook secret key is missing or invalid for current merchant. Attempting fallback to parent merchant...");

                Long parentId = null;
                Long merchantId = paymentLink.getCreatedBy();
                String invoiceId = paymentLink.getInvoiceId();

                log.debug("Initial merchantId: {}, invoiceId: {}", merchantId, invoiceId);

                if (merchantId != null && merchantId > 0) {
                    if (invoiceId.startsWith(merchantId + "")) {
                        log.debug("Invoice ID starts with merchant ID ({}). Treating as sub-merchant.", merchantId);

                        MerchantEntity subUser = merchantRepository.findById(merchantId).orElse(null);
                        if (subUser != null && subUser.getParentMerchant() != null) {
                            parentId = subUser.getParentMerchant().getId();
                            log.debug("Sub-merchant has parent. Using parent merchant ID: {}", parentId);
                        } else {
                            parentId = merchantId;
                            log.debug("No parent found. Using merchant ID as fallback: {}", parentId);
                        }
                    } else {
                        parentId = merchantId;
                        log.debug("Invoice ID does not start with merchant ID. Using merchant ID: {}", parentId);
                    }

                    if (parentId != null) {
                        MerchantServiceConfigEntity config = merchantServiceConfigRepository.findByMerchantId(parentId).orElse(null);
                        if (config != null) {
                            //Load secret key from config
                            if (secretKey == null || secretKey.isBlank() || secretKey.isEmpty() || "null".equals(secretKey) )  {
                                secretKey = config.getWebhookSecretKey();
                                log.info("Webhook secret key successfully fetched from parent merchant ID: {}", parentId);
                            }
                            //Load webhook URL from config
                            if(url==null || url.isEmpty() || url.isBlank())   {
                                url = config.getWebhookUrl();
                                log.info("Webhook url successfully fetched from parent merchant ID: {}", parentId);
                            }
                        } else {
                            log.warn("No webhook config found for parent merchant with ID: {}", parentId);
                        }
                    }
                } else {
                    log.warn("Merchant ID is null or invalid. Cannot resolve parent webhook secret key.");
                }
            }

            WebhookLogEntity entityLog = new WebhookLogEntity();
            entityLog.setWebhookUrl(url);
            entityLog.setTimestamp(LocalDateTime.now());
            try {

                if (paymentLink.getCreatedBy() != null) {

                    Long parentMerchantId=paymentLink.getCreatedBy();
                    Optional<MerchantServiceConfigEntity> config = merchantServiceConfigRepository.findByMerchantId(parentMerchantId);
                    if (config.isPresent()) {
                        if (url == null || url.isBlank() || url.isEmpty())
                            url = config.get().getWebhookUrl();

                        if (secretKey == null || secretKey.isBlank() || secretKey.isEmpty())
                            secretKey = config.get().getWebhookSecretKey();
                    }

                }
                entityLog.setWebhookUrl(url);
                //log.setWebhookSecretKey(secretKey);

                String jsonPayload = objectMapper.writeValueAsString(response);


                entityLog.setRequestPayload(jsonPayload);
                entityLog.setInvoiceId(paymentLink.getInvoiceId());
                entityLog.setPaymentId(paymentLink.getPaymentId());

                String signature = "";


                if (secretKey != null) {

                    String stringToSign = buildSelectiveSignatureString(response, signedAttributes);
                    entityLog.setFlatPayloadPriorSign(stringToSign);
                    signature = computeHmacSHA256(stringToSign, secretKey);
                }
                entityLog.setSignature(signature);
                log.debug("Sending webhook to {}",url);
                log.debug("Body {}",jsonPayload);
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("SignedAttributes", String.join(",", signedAttributes));
                connection.setRequestProperty("Signature", signature);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                entityLog.setResponseCode(responseCode);

                String responseBody = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                entityLog.setResponseBody(responseBody);
                entityLog.setStatus(responseCode == 200 || responseCode == 202 ? "SUCCESS" : "FAILED");

            } catch (Exception e) {
                entityLog.setResponseCode(500);
                entityLog.setStatus("FAILED");
                entityLog.setResponseBody(e.getMessage());
            } finally {
                webhookLogRepository.save(entityLog);
            }
        }    }

    public String buildSelectiveSignatureString(Object payload, List<String> orderedFieldPaths) {
        Map<String, Object> jsonMap = objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {});
        List<String> values = new ArrayList<>();
        for (String path : orderedFieldPaths) {
            Object val = extractValueByPath(jsonMap, path);
            values.add(path + "=" + (val != null ? val.toString() : null));
        }
        return String.join("&", values);
    }

    @SuppressWarnings("unchecked")
    private Object extractValueByPath(Map<String, Object> jsonMap, String path) {
        String[] parts = path.split("\\.");
        Object current = jsonMap;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }



    private String computeHmacSHA256(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKey, "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash); // Or Hex.encodeHexString(hash)
    }
}

