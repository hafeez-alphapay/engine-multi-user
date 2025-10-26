package com.alphapay.payEngine.storage.service;

import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import com.alphapay.payEngine.storage.model.MerchantDocuments;
import com.alphapay.payEngine.account.management.model.UserEntity;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    MerchantDocuments store(MultipartFile file, MerchantEntity merchantUser, UserEntity uploadedBy, String type, Long documentCategoryId)  throws Exception;
    String storeCustomerDoc(String documentType, MultipartFile file, String invoiceId) throws Exception;
}
