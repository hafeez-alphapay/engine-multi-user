package com.alphapay.payEngine.controller.alphaServices;

import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.alphaServices.historyTransaction.dto.request.TransactionHistoryRequest;
import com.alphapay.payEngine.alphaServices.historyTransaction.service.TransHistoryService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/history")
public class TransHistoryController {

    @Autowired
    private TransHistoryService transHistoryService;

    @RequestMapping(value = "/transactions", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public PaginatedResponse<FinancialTransaction> generateGenericPaymentLink(@Valid @RequestBody TransactionHistoryRequest request) {
        return transHistoryService.getAllExecutePaymentTransaction(request);
    }

    @PostMapping("/transactions/summary")
    public List<Map<String, Object>> getTransactionSummary(@RequestBody TransactionHistoryRequest request) {
        return transHistoryService.getTransactionSummary(request);
    }

}
