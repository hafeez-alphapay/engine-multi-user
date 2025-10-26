package com.alphapay.payEngine.alphaServices.service;

import com.alphapay.payEngine.alphaServices.dto.request.ExportTransactionRequest;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface ExportDataService {
      File generateExcelFile(ExportTransactionRequest request) throws IOException ;
}
