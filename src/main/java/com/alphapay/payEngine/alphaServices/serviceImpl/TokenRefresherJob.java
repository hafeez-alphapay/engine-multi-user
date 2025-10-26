package com.alphapay.payEngine.alphaServices.serviceImpl;


import com.alphapay.payEngine.alphaServices.model.IntegrationApiToken;
import com.alphapay.payEngine.alphaServices.repository.IntegrationApiTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class TokenRefresherJob {

    private final IntegrationApiTokenRepository repo;
    private final IntegrationTokenService tokenService;
    private final Clock clock = Clock.systemUTC();

    // default proactive window if issuer not yet cached (fallback)
    private final int defaultRenewWindowSeconds = 5*60*60; // 5 HOURS PRIOR

    public TokenRefresherJob(IntegrationApiTokenRepository repo, IntegrationTokenService tokenService) {
        this.repo = repo;
        this.tokenService = tokenService;
    }

    /**
     * Every x ms : find tokens expiring within the next N seconds and refresh.
     * You can tune via application.yml: tokens.scheduler.fixedDelay, tokens.scheduler.renewWindowSeconds
     */
    @Scheduled(fixedDelayString = "${tokens.scheduler.fixedDelay:60000}")
    public void refreshExpiringSoon() {
        log.debug("refreshExpiringSoon() started to refresh tokens in db");

        Instant now = Instant.now(clock);
        int renewWindow = Integer.getInteger("tokens.scheduler.renewWindowSeconds", defaultRenewWindowSeconds);
        Instant threshold = now.plusSeconds(renewWindow);

        List<IntegrationApiToken> expiring = repo.findExpiringBefore(threshold);
        int refreshed = 0;

        for (IntegrationApiToken row : expiring) {
            try {
                tokenService.forceRefreshAndGet(row.getWorkFlowId(), row.getTokenName());
                refreshed++;
                log.info("Refreshed token for {}/{} exp={}", row.getWorkFlowId(), row.getTokenName(), row.getExpiresAtUtc());
            } catch (Exception e) {
                log.warn("Token refresh failed for {}/{}: {}", row.getWorkFlowId(), row.getTokenName(), e.getMessage(), e);
            }
        }

        log.debug("refreshExpiringSoon() finished. Refreshed {} tokens out of {}", refreshed, expiring.size());
    }
}
