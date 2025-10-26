package com.alphapay.payEngine.transactionLogging.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Setter
@Getter
public class TransactionSummaryResponse {
    private BigDecimal totalAmount;
    private BigDecimal successfulAmount;
    private Map<String, Integer> totalSuccessfulTransactions;
    private Map<String, Integer> totalFailedTransactions;
    private Map<String, Integer> totalPendingTransactions;
}
