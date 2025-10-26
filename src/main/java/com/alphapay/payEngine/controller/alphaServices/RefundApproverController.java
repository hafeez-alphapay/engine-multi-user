package com.alphapay.payEngine.controller.alphaServices;

import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.alphaServices.dto.request.ApproveRefundRequest;
import com.alphapay.payEngine.alphaServices.dto.request.GetPendingRefundRequest;
import com.alphapay.payEngine.alphaServices.model.PendingRefundProcess;
import com.alphapay.payEngine.alphaServices.service.RefundApprovalService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/refundApprover")
public class RefundApproverController {

    @Autowired
    RefundApprovalService refundApprovalService;

    @RequestMapping(value = "/approve", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public FinancialTransaction approve(@RequestBody @Valid ApproveRefundRequest approveRefundRequest) {
        return refundApprovalService.approveRefund(approveRefundRequest);
    }

    @RequestMapping(value = "/pending", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public PaginatedResponse<PendingRefundProcess> getPending(@RequestBody @Valid GetPendingRefundRequest getPendingRefundRequest){
        return refundApprovalService.getPendingRefunds(getPendingRefundRequest);
    }



}
