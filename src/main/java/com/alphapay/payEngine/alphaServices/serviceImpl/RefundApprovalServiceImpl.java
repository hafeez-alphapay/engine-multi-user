package com.alphapay.payEngine.alphaServices.serviceImpl;

import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.alphaServices.dto.request.ApproveRefundRequest;
import com.alphapay.payEngine.alphaServices.dto.request.GetPendingRefundRequest;
import com.alphapay.payEngine.alphaServices.exception.RefundAlreadyApprovedException;
import com.alphapay.payEngine.alphaServices.exception.RefundIDNotFoundException;
import com.alphapay.payEngine.alphaServices.model.PendingRefundProcess;
import com.alphapay.payEngine.alphaServices.repository.PendingRefundProcessRepository;
import com.alphapay.payEngine.alphaServices.service.RefundApprovalService;
import com.alphapay.payEngine.integration.dto.request.RefundRequest;
import com.alphapay.payEngine.financial.service.FinancialTransactionLedgerService;
import com.alphapay.payEngine.integration.service.InitiatePaymentService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RefundApprovalServiceImpl implements RefundApprovalService {

    @Autowired
    PendingRefundProcessRepository pendingRefundProcessRepository;

    @Autowired
    FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    FinancialTransactionLedgerService financialTransactionLedgerService;

    @Autowired
    InitiatePaymentService initiatePaymentService;

    @Value("${trans.history.page.size}")
    private String historyPageSize;
    @Override
    public PaginatedResponse<PendingRefundProcess> getPendingRefunds(GetPendingRefundRequest request){
        Specification<PendingRefundProcess> transactionSpec = new PendingRefundTransactionSpecification("PendingApproval");
        int pageIndex = request.getPageNumber() == null ? 0 : request.getPageNumber() - 1;
        int pageSize = request.getPageSize() == null ? Integer.parseInt(historyPageSize) : request.getPageSize();
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("creationTime").descending());
        Page<PendingRefundProcess> refunds;
        try {
        refunds = pendingRefundProcessRepository.findAll(transactionSpec, pageRequest);
         } catch (Exception ex) {
        log.error("Error fetching payment link data", ex);
        throw new RuntimeException("Error fetching payment link data");
        }
        List<PendingRefundProcess> responseList = refunds.stream().collect(Collectors.toList());
        return new PaginatedResponse<>(responseList, refunds.getNumber() + 1, // Convert zero-based page index to one-based
                refunds.getSize(), refunds.getTotalElements(), refunds.getTotalPages(), refunds.isLast());

        // pendingRefundProcessRepository.findByStatus("PENDING_APPROVAL");
    }

    @Override
    public FinancialTransaction approveRefund(ApproveRefundRequest request) {
        PendingRefundProcess refundProcess = pendingRefundProcessRepository.findByAlphaRefundId(request.getRefundId())
                .orElseThrow(() -> new RefundIDNotFoundException());

        if("APPROVED".equals(refundProcess.getStatus()))
        {
            log.error("Refund ID: {} has already been approved.", refundProcess.getAlphaRefundId());
            throw new RefundAlreadyApprovedException();
        }


        //Process Refund
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setKey(refundProcess.getKey());
        refundRequest.setKeyType(refundProcess.getKeyType());
        refundRequest.setComment(refundProcess.getComment());
        refundRequest.setSupplierDeductedAmount(refundProcess.getSupplierDeductedAmount());
        refundRequest.setRequestId(refundProcess.getRequestId());


        FinancialTransaction ft=initiatePaymentService.processRefund(refundRequest);
        log.debug("Refund Processed, Financial Transaction: {}", ft);
        if(ft!=null && !"Failed".equals(ft.getTransactionStatus()) && ft.getPaymentResponse()!=null){
            if(ft.getPaymentResponse().containsKey("refundId"))
            {
                String externalRefundId= (String) ft.getPaymentResponse().get("refundId");
                if(externalRefundId!=null)
                {
                    refundProcess.setStatus("APPROVED");
                    refundProcess.setApprovedBy(request.getApprovedBy());
                    refundProcess.setApprovedById(request.getApprovedById());
                    refundProcess.setApprovalComments(request.getApprovalComments());
                    pendingRefundProcessRepository.save(refundProcess);
                    updateTransactionLog(refundProcess.getAlphaRefundId(),externalRefundId,refundProcess.getRequestId());

                }
            }
        }
        else
        {
            log.error("Refund processing failed for Refund ID: {}, Financial Transaction: {}", refundProcess.getAlphaRefundId(), ft);
        }
        assert ft != null;
        ft.setRequestId(request.getRequestId());
        return ft;

    }

    @Transactional
    void updateTransactionLog(String refundId, String externalRefundId,String requestId  )
    {
        FinancialTransaction ft = financialTransactionRepository.findByRequestId(requestId);
        if(ft!=null)
        {
            try{
            ft.setRefundId(refundId);
            ft.setExternalRefundId(externalRefundId);
            financialTransactionLedgerService.save(ft);}
            catch (Exception e)
            {
                log.error("Error updating FinancialTransaction for requestId: {}, error: {}", requestId, e.getMessage());
            }
        }
    }
}
