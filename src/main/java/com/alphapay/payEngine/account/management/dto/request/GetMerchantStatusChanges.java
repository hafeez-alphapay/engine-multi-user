package com.alphapay.payEngine.account.management.dto.request;


import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetMerchantStatusChanges extends BaseRequest {
    private Long merchantId;
    private Long assignedUserId;
    private Long performByUserId;
    private Integer pageNumber;
    private Integer pageSize;
    private LocalDateTime performedAt;
}
