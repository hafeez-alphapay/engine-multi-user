package com.alphapay.payEngine.controller.account;

import com.alphapay.payEngine.account.management.dto.request.*;
import com.alphapay.payEngine.account.management.dto.response.*;
import com.alphapay.payEngine.account.management.model.MerchantComment;
import com.alphapay.payEngine.account.management.model.MerchantProviders;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.service.MerchantKycService;
import com.alphapay.payEngine.account.merchantKyc.dto.KycOnboardingRequest;
import com.alphapay.payEngine.account.merchantKyc.dto.KycOnboardingResponse;
import com.alphapay.payEngine.account.merchantKyc.service.KycOnboardingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class MerchantKycController {

    @Autowired
    private MerchantKycService merchantKycService;

    @Autowired
    private KycOnboardingService kycOnboardingService;

    @RequestMapping(value = "/merchant/all", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<PaginatedMerchantResponse<MerchantResponse>> getAllMerchantRegistrations(@Valid @RequestBody GetAllUsersRequestFilter request) {

        PaginatedMerchantResponse<MerchantResponse> response = merchantKycService.getAllMerchants(request);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/merchant/details", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<MerchantDetailsResponse> getMerchantDetails(@Valid @RequestBody MerchantDetailsRequest request) {

        MerchantDetailsResponse response = merchantKycService.getMerchantFullDetails(request);
        return ResponseEntity.ok(response);
    }


    @RequestMapping(value = "/merchant/changeAccountStatus", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public MerchantStatusResponse changeMerchantAccountStatus(@Valid @RequestBody MerchantAccountStatusRequest request) {
        return merchantKycService.changeMerchantAccountStatus(request);
    }

    @RequestMapping(value = "/assignMerchant", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public void assignMerchant(@Valid @RequestBody AssignMerchantRequest request) {
        merchantKycService.assignMerchant(request);
    }

    @RequestMapping(value = "/addMerchantComment", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public List<MerchantComment> addComment(@Valid @RequestBody AddCommentMerchantRequest request) {
        return merchantKycService.addCommentMerchant(request);
    }

    @RequestMapping(value = "/getMerchantComment", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public MerchantCommentResponse getMerchantComment(@Valid @RequestBody AddCommentMerchantRequest request) {
        return merchantKycService.AllCommentMerchant(request);
    }

    @RequestMapping(value = "/assignAggregatorId", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public void assignMerchantAggregatorId(@Valid @RequestBody MerchantAggregatorLinkRequest request) {
        merchantKycService.assignMerchantAggreagtorId(request);
    }

    @RequestMapping(value = "/updateAggregatorStatus", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public void updateClientAggregatorStatus(@Valid @RequestBody MerchantAggregatorLinkRequest request) {
        merchantKycService.updateClientAggregatorStatus(request);
    }

    @RequestMapping(value = "/vendor/merchants", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public List<VendorMerchants> getParentMerchantWithSubMerchant(@Valid @RequestBody MerchantDetailsRequest request) {
        return merchantKycService.getParentMerchantWithSubMerchant(request);
    }

    @PostMapping("/merchant/onboard")
    public KycOnboardingResponse onboard(@Valid @RequestBody KycOnboardingRequest request) {
        return kycOnboardingService.onboardMerchant(request);
    }

    @PostMapping("/merchant/withProvider")
    public List<MerchantProviders> getMerchantProvider(@Valid @RequestBody MerchantDetailsRequest request) {
        return merchantKycService.getMerchantProvider(request);
    }

}