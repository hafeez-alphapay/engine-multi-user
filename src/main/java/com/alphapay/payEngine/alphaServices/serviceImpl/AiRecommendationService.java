package com.alphapay.payEngine.alphaServices.serviceImpl;


import com.alphapay.payEngine.alphaServices.historyTransaction.dto.response.TransactionStats;

import com.alphapay.payEngine.alphaServices.dto.response.MerchantStats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Generates natural‑language recommendations for the daily report
 * using OpenAI Chat Completions. Falls back to template text if the API is unavailable.
 */
@Service
public class AiRecommendationService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    /**
     * Build 3–5 actionable recommendations from the provided stats.
     */
    public String generateInsights(TransactionStats txStats, MerchantStats merchantStats) {
        // Fallback if no API key configured
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return fallbackText(txStats, merchantStats, "OpenAI API key not configured");
        }

        try {
            // Build a compact JSON payload with just what the model needs
            ObjectNode root = mapper.createObjectNode();
            root.set("transactions", mapper.valueToTree(txStats));
            root.set("merchants", mapper.valueToTree(merchantStats));

            String systemMsg =
                    "You are a senior consultant payments analyst has more than 30 years of experiences in fintech sector in Stripe, PayPal, Authorize.Net, Adyen and Block (Square & Cash App). Based on the provided metrics, write 3–7 short, actionable recommendations " +
                            "to improve payment success, reduce failures, speed merchant onboarding, and optimize operations. " +
                            "Additionally, include strategies to maximize revenue given that our commission is a fixed 1 AED per transaction " +
                            "plus a percentage between 1.5% and 0.2% of the total amount, depending on the transaction. " +
                            "Suggest ways to increase the number of transactions and the average transaction amount to boost revenue. " +
                            "Also, provide creative ideas to increase merchant engagement with our platform, encouraging them to use our payment link feature " +
                            "and payment gateway more frequently. " +
                            "Be specific, avoid generic advice, and reference concrete signals from the provided data.";
            String userMsg = "Metrics JSON:\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("model", openAiModel);
            rootNode.set("messages", mapper.createArrayNode()
                    .add(mapper.createObjectNode()
                            .put("role", "system")
                            .put("content", systemMsg))
                    .add(mapper.createObjectNode()
                            .put("role", "user")
                            .put("content", userMsg)));
            rootNode.put("temperature", 0.3);
            rootNode.put("max_tokens", 400);

            String requestBody = mapper.writeValueAsString(rootNode);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                // Parse assistant message
                var tree = mapper.readTree(resp.body());
                var choices = tree.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    var content = choices.get(0).path("message").path("content").asText();
                    if (content != null && !content.isBlank()) return content.trim();
                }
                return fallbackText(txStats, merchantStats, "Empty completion content");
            } else {
                return fallbackText(txStats, merchantStats,
                        "OpenAI HTTP " + resp.statusCode() + ": " + resp.body());
            }
        } catch (Exception ex) {
            return fallbackText(txStats, merchantStats, ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private String fallbackText(TransactionStats tx, MerchantStats ms, String reason) {
        int total = tx.getTotalCount();
        int succ = tx.getSuccessCount();
        int fail = tx.getFailCount();
        int pend = tx.getPendingCount();
        int inProg = tx.getInProgressCount();

        double successRate = total > 0 ? (succ * 100.0 / total) : 0.0;
        double failRate = total > 0 ? (fail * 100.0 / total) : 0.0;

        double avgApprovalHrs = ms.getAvgApprovalHours();
        int approvedToday = ms.getApprovedToday();
        int lastLoginCount = ms.getLastLoginCount();
        int lockedCount = ms.getLockedCount();
        int disabledCount = ms.getDisabledCount();

        String topFailReason = (tx.getFailureReasons() != null && !tx.getFailureReasons().isEmpty())
                ? tx.getFailureReasons().entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .findFirst().map(e -> e.getKey() + " (" + e.getValue() + ")").orElse("N/A")
                : "N/A";

        return String.join("\n",
                "AI recommendations (fallback — " + reason + "):",
                "",
                String.format("1) Success rate is %.1f%% (fail %.1f%%). Prioritize fixing the top failure cause: %s.",
                        successRate, failRate, topFailReason),
                String.format("2) %d payments pending and %d in progress. Add auto-status polling + reminder at 5–10 minutes and auto-cancel policy.", pend, inProg),
                "3) Enable front-end validation for mandatory fields (e.g., item name, CVV length) and hide unavailable payment methods dynamically.",
                String.format("4) Merchant approvals average %.1f hours with %d approved today. Recently logged-in merchants: %d; locked accounts: %d; disabled accounts: %d. Automate KYC checks and add SLA alerts for reviewers.",
                        avgApprovalHrs, approvedToday, lastLoginCount, lockedCount, disabledCount),
                "5) If 3‑D Secure failures are common, coordinate with the acquirer on enrollment issues and provide clearer UX guidance on the OTP step."
        );
    }

}