package com.alphapay.payEngine.controller.alphaServices;

import com.alphapay.payEngine.alphaServices.dto.request.ExportTransactionRequest;
import com.alphapay.payEngine.alphaServices.dto.response.MerchantStats;
import com.alphapay.payEngine.alphaServices.historyTransaction.dto.response.TransactionStats;
import com.alphapay.payEngine.alphaServices.historyTransaction.service.TransHistoryService;
import com.alphapay.payEngine.alphaServices.service.ExportDataService;
import com.alphapay.payEngine.alphaServices.service.MerchantAlphaPayServicesService;
import com.alphapay.payEngine.alphaServices.serviceImpl.AiRecommendationService;
import com.alphapay.payEngine.notification.services.INotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/export")
public class ExportDataController {

    @Autowired
    private ExportDataService exportTransactions;

    @RequestMapping(value = "/transactions", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<Resource> exportTransactions(@RequestBody @Valid ExportTransactionRequest request) throws IOException {
        File excelFile = exportTransactions.generateExcelFile(request);
        Path path = Paths.get(excelFile.getAbsolutePath());
        Resource resource = new UrlResource(path.toUri());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        excelFile.deleteOnExit();
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelFile.length())
                .body(resource);
    }

}
