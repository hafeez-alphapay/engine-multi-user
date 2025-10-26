package com.alphapay.payEngine.account.merchantKyc.service;


import com.alphapay.payEngine.account.merchantKyc.dto.KycOnboardingRequest;
import com.alphapay.payEngine.account.merchantKyc.dto.KycOnboardingResponse;
import com.alphapay.payEngine.model.response.BaseResponse;

/**
 * Service interface for handling KYB onboarding logic.
 */
public interface KycOnboardingService {

    /**
     * Processes a new KYB onboarding request for a merchant user.
     *
     * @param request the KYC onboarding request containing all merchant and manager details
     */
    KycOnboardingResponse onboardMerchant(KycOnboardingRequest request);
}