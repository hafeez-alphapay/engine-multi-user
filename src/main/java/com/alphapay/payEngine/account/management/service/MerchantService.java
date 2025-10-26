package com.alphapay.payEngine.account.management.service;

import com.alphapay.payEngine.account.management.dto.request.GetAllUsersRequestFilter;
import com.alphapay.payEngine.account.merchantKyc.model.MerchantEntity;
import org.springframework.data.domain.Page;

public interface MerchantService {
    Page<MerchantEntity> getAllMerchants(GetAllUsersRequestFilter request);
    Page<MerchantEntity> getMultiVendorUsers(GetAllUsersRequestFilter request);

    MerchantEntity getMerchant(Long merchantId);
    MerchantEntity getMerchantByUserId(Long userId);
}
