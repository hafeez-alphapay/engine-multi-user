package com.alphapay.payEngine.controller.account;

import com.alphapay.payEngine.account.management.dto.request.GetMerchantStatusChanges;
import com.alphapay.payEngine.account.management.dto.request.UpdateMerchantStatusRequest;
import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.account.management.model.MerchantStatusHistoryEntity;
import com.alphapay.payEngine.account.management.service.MerchantStatusControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statusControl")
public class MerchantStatusControlController {
    @Autowired
    private MerchantStatusControlService merchantStatusControlService;


    @PostMapping("/update")
    public MerchantStatusHistoryEntity updateMerchantStatus(@RequestBody UpdateMerchantStatusRequest request) {
       return merchantStatusControlService.handleStatusChange(request);
    }

    @PostMapping("/history/filter")
    public PaginatedResponse filterStatusHistory(@RequestBody GetMerchantStatusChanges request) {
        return merchantStatusControlService.filterStatusChanges(request);
    }
}
