package com.alphapay.payEngine.account.management.dto.response;

import lombok.Getter;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
public class InvoiceTypeSummaryResponse {
    private String type;
    private List<InvoiceStatusSummary> statuses;

    public InvoiceTypeSummaryResponse(String type, List<InvoiceStatusSummary> statuses) {
        this.type = type;
        this.statuses = statuses;
    }
}