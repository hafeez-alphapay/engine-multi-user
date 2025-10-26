package com.alphapay.payEngine.alphaServices.service;

import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.alphaServices.dto.request.ApproveRefundRequest;
import com.alphapay.payEngine.alphaServices.dto.request.GetPendingRefundRequest;
import com.alphapay.payEngine.alphaServices.model.PendingRefundProcess;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;

import java.util.List;

public interface RefundApprovalService {

    PaginatedResponse<PendingRefundProcess> getPendingRefunds(GetPendingRefundRequest request);

    FinancialTransaction approveRefund(ApproveRefundRequest request);
}
