package com.alphapay.payEngine.alphaServices.historyTransaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class TransactionStats {
    private int totalCount;
    private double totalAmount;
    private int successCount;
    private double successAmount;
    private double minCommission;
    private double maxCommission;
    private int failCount;
    private double failAmount;
    private int pendingCount;
    private int inProgressCount;
    private Map<String, Integer> failureReasons;

    public TransactionStats(int totalCount, double totalAmount, int successCount, double successAmount,
                            int failCount, double failAmount, int pendingCount, int inProgressCount,
                            Map<String, Integer> failureReasons,double minCommission,double maxCommission) {
        this.totalCount = totalCount;
        this.totalAmount = totalAmount;
        this.successCount = successCount;
        this.successAmount = successAmount;
        this.failCount = failCount;
        this.failAmount = failAmount;
        this.pendingCount = pendingCount;
        this.inProgressCount = inProgressCount;
        this.failureReasons = failureReasons;
        this.maxCommission = maxCommission;
        this.minCommission = minCommission;
    }

    // Getters and setters...
}