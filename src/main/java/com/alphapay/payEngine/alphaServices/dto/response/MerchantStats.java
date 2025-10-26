package com.alphapay.payEngine.alphaServices.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * DTO summarizing merchant stats for the last 24 hours.
 * Matches constructor usage in MerchantAlphaPayServicesServiceImpl#getLast24hStats().
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantStats implements Serializable {

    // High-level counts (last 24h window)
    private int newMerchantsToday;
    private int approvedToday;
    private int lastLoginCount;
    private int rejectedToday;

    // Durations
    private double avgApprovalHours;   // avg hours from creation to activation for approvals in window

    // Approval-stage breakdowns (counts per status value)
    private Map<String, Long> managerApprovalBreakdown;   // e.g., {"Approved": 4, "Pending": 2}
    private Map<String, Long> adminApprovalBreakdown;     // e.g., {"Approved": 3, "Rejected": 1}
    private Map<String, Long> mbmeApprovalBreakdown;      // PSP-specific stage
    private Map<String, Long> myfattoraApprovalBreakdown; // PSP-specific stage

    // Account state counts in the window
    private int lockedCount;   // merchants with locked=true
    private int disabledCount; // merchants with disabledDate != null
}