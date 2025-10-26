package com.alphapay.payEngine.account.customerKyc.service;

import com.alphapay.payEngine.account.customerKyc.dto.response.CustomerIdVerificationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CustomerKycVerificationService {
    CustomerIdVerificationResponse verifyCustomerIdDocument(String idType, String invoiceId, MultipartFile file, String requestId, String acceptLanguage);
}
