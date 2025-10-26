package com.alphapay.payEngine.storage.service;

import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.storage.dto.DocumentDescription;
import com.alphapay.payEngine.storage.dto.MerchantDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    List<DocumentDescription> getActiveRequireDocuments();

    MerchantDocumentResponse uploadDocument(Long documentCategoryId, Long merchantId, MultipartFile file, String requestId, Long userId) throws Exception;
    List<MerchantDocumentResponse> findMerchantDocByMerchantId(Long merchantId);

    PaymentLinkEntity uploadCustomerDoc(String documentType,String invoiceId, MultipartFile file, String requestId) throws Exception;
}
