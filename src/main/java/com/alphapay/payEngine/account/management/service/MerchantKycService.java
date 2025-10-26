package com.alphapay.payEngine.account.management.service;

import com.alphapay.payEngine.account.management.dto.request.*;
import com.alphapay.payEngine.account.management.dto.response.*;
import com.alphapay.payEngine.account.management.model.MerchantComment;
import com.alphapay.payEngine.account.management.model.MerchantProviders;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.storage.dto.MerchantDocumentResponse;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MerchantKycService {
    PaginatedMerchantResponse<MerchantResponse> getAllMerchants(GetAllUsersRequestFilter request);

    MerchantDocumentResponse uploadDocument(Long documentCategoryId, Long merchantId, MultipartFile file, String requestId, String acceptLanguage, Long userId) throws Exception;

    MerchantStatusResponse changeMerchantAccountStatus(@Valid MerchantAccountStatusRequest request);

    MerchantDetailsResponse getMerchantDetails(@Valid MerchantDetailsRequest request);

    void assignMerchant(@Valid AssignMerchantRequest request);

    List<MerchantComment> addCommentMerchant(@Valid AddCommentMerchantRequest request);

    MerchantCommentResponse AllCommentMerchant(@Valid AddCommentMerchantRequest request);

      PaymentLinkEntity uploadSignDocument(String invoiceId, MultipartFile file, String requestId) throws Exception;

    void assignMerchantAggreagtorId(MerchantAggregatorLinkRequest request);
    void updateClientAggregatorStatus(MerchantAggregatorLinkRequest request);
    MerchantDetailsResponse getMerchantFullDetails(MerchantDetailsRequest request);

    List<VendorMerchants> getParentMerchantWithSubMerchant(@Valid MerchantDetailsRequest request);
    List<MerchantProviders> getMerchantProvider(MerchantDetailsRequest request);
}
