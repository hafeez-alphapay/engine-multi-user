package com.alphapay.payEngine.account.management.service;

import com.alphapay.payEngine.account.management.dto.request.GetMerchantStatusChanges;
import com.alphapay.payEngine.account.management.dto.request.UpdateMerchantStatusRequest;
import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.account.management.model.MerchantStatusHistoryEntity;

public interface MerchantStatusControlService {
    MerchantStatusHistoryEntity handleStatusChange(UpdateMerchantStatusRequest request);
    PaginatedResponse filterStatusChanges(GetMerchantStatusChanges request);
}
