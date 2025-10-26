package com.alphapay.payEngine.account.management.dto.request;


import com.alphapay.payEngine.service.bean.BaseRequest;
import com.alphapay.payEngine.utilities.MerchantStatusEntityType;
import lombok.Data;

@Data
public class UpdateMerchantStatusRequest extends BaseRequest {
    private MerchantStatusEntityType entityType;         // USER_ACCOUNT, MANAGER_APPROVAL, etc.
    private Long merchantId;
    private String newStatus;
    private String comment;
    private Long assignedUserId;
    private Long performByUserId;
}
