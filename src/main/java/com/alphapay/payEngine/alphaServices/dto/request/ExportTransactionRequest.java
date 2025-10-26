package com.alphapay.payEngine.alphaServices.dto.request;

import com.alphapay.payEngine.alphaServices.historyTransaction.dto.request.TransactionHistoryRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ExportTransactionRequest extends TransactionHistoryRequest {
    private List<String> columns;
    private String format;
    private String reportType;
}
