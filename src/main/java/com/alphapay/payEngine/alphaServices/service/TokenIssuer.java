package com.alphapay.payEngine.alphaServices.service;

import com.alphapay.payEngine.alphaServices.model.IntegrationApiToken;

import java.time.Duration;

public interface TokenIssuer {
    /** Returns a freshly issued raw JWT (or token string) for this integration. */
    String issue(IntegrationApiToken token);
    /** Optional: how long before exp we should refresh */
    default Duration renewWindow() { return Duration.ofHours(5); }
}
