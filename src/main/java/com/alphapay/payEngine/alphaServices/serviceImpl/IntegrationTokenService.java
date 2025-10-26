package com.alphapay.payEngine.alphaServices.serviceImpl;


import com.alphapay.payEngine.alphaServices.model.IntegrationApiToken;
import com.alphapay.payEngine.alphaServices.repository.IntegrationApiTokenRepository;
import com.alphapay.payEngine.alphaServices.service.TokenIssuer;
import com.alphapay.payEngine.common.token.JwtTimes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class IntegrationTokenService {

    private final IntegrationApiTokenRepository repo;
    private final TokenIssuer issuer;
    private final Clock clock = Clock.systemUTC();

    private static final Duration CLOCK_SKEW = Duration.ofHours(24);

    private static final class Entry {
        volatile String token;
        volatile Instant exp;
        volatile int renewWindowSeconds = 300;
        final ReentrantLock lock = new ReentrantLock();
    }
    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>();

    public IntegrationTokenService(IntegrationApiTokenRepository repo, TokenIssuer issuer) {
        this.repo = repo;
        this.issuer = issuer;
        // Warm cache from DB
        repo.findAll().forEach(row -> {
            Entry e = cache.computeIfAbsent(key(row), k -> new Entry());
            e.token = row.getAccessToken();
            e.exp   = row.getExpiresAtUtc();
        });
    }

    private static String key(IntegrationApiToken row) {
        return row.getWorkFlowId() + ":" + row.getTokenName();
    }
    private static String key(String wf, String tn) { return wf + ":" + tn; }

    /** Get a valid token using workflowId + tokenName (headers from client). */
    public String getValidToken(String workFlowId) {
        var row = Objects.requireNonNull(
                repo.findOne(workFlowId, workFlowId),
                "Unknown workflow/tokenName");
        var k = key(row);
        var e = cache.computeIfAbsent(k, kk -> new Entry());
        Instant now = Instant.now(clock);

        if (e.token != null && e.exp != null &&
                e.exp.isAfter(now.plus(CLOCK_SKEW)) &&
                e.exp.isAfter(now.plusSeconds(e.renewWindowSeconds))) {
            if(e.token.equals(row.getAccessToken()))
                return e.token;
            return row.getAccessToken();
        }
        return doRefresh(row, e, now);
    }

    public String forceRefreshAndGet(String workFlowId, String tokenName) {
        var row = repo.findOne(workFlowId, tokenName);
        if (row == null) throw new IllegalArgumentException("Unknown workflow/tokenName");
        return doRefresh(row, cache.computeIfAbsent(key(row), k -> new Entry()), Instant.now(clock));
    }

    private String doRefresh(IntegrationApiToken row, Entry e, Instant now) {
        e.lock.lock();
        try {
            // Double-check: maybe another thread refreshed already
            if (e.token != null && e.exp != null &&
                    e.exp.isAfter(now.plusSeconds(e.renewWindowSeconds))) {
                return e.token;
            }

          //  RefreshToken
            String newToken = issuer.issue(row);

            Instant iat, exp;
            try {
                var t = JwtTimes.parse(newToken);
                iat = t.iat(); exp = t.exp();
            } catch (Exception notJwt) {
                notJwt.printStackTrace();
                // Non-JWT; pick a safe TTL policy (e.g., 30 minutes)
                iat = now; exp = now.plus(Duration.ofMinutes(10));
            }

            //e.renewWindowSeconds = issuer.renewWindowSeconds();
            e.token = newToken; e.exp = exp;

            upsert(row, newToken, iat, exp, now);

            return newToken;
        } finally {
            e.lock.unlock();
        }
    }

    @Transactional
    protected void upsert(IntegrationApiToken row, String token, Instant iat, Instant exp, Instant now) {
        row.setAccessToken(token);
        row.setIssuedAtUtc(iat);
        row.setExpiresAtUtc(exp);
        row.setLastUpdatedUtc(now);
        repo.saveAndFlush(row); // optimistic @Version on the entity protects from races
    }
}