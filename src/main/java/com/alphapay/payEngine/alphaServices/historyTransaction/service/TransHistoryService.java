package com.alphapay.payEngine.alphaServices.historyTransaction.service;

import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.alphaServices.historyTransaction.dto.request.TransactionHistoryRequest;
import com.alphapay.payEngine.alphaServices.historyTransaction.dto.response.TransactionStats;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;

import java.util.List;
import java.util.Map;

public interface TransHistoryService {
    PaginatedResponse<FinancialTransaction> getAllExecutePaymentTransaction(TransactionHistoryRequest request);

    List<Map<String, Object>> getTransactionSummary(TransactionHistoryRequest request);

 }
