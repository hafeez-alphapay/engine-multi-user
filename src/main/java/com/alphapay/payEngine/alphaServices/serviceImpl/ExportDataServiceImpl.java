package com.alphapay.payEngine.alphaServices.serviceImpl;

import com.alphapay.payEngine.account.management.exception.DateMismatchException;
import com.alphapay.payEngine.alphaServices.dto.request.ExportTransactionRequest;
import com.alphapay.payEngine.alphaServices.dto.response.CustomerCardBINInfoResponse;
import com.alphapay.payEngine.alphaServices.historyTransaction.serviceImpl.TransactionHistorySpecification;
import com.alphapay.payEngine.alphaServices.service.ExportDataService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class ExportDataServiceImpl implements ExportDataService {

    @Autowired
    private FinancialTransactionRepository transactionRepository;

    @Value("${trans.history.page.size}")
    private String historyPageSize;

    @Value("${trans.history.duration}")
    private String transHistoryDuration;
    @Autowired
    private BINServiceImpl binService;

    @Override
    public File generateExcelFile(ExportTransactionRequest request) throws IOException {
        request.setTransactionType("ExecutePaymentRequest,DirectPaymentRefundRequest,PortalRefundRequest");
        if (request.getToDate() != null)
            if (request.getToDate().before(request.getFromDate())) {
                log.debug("Date mismatch: To date {} is before From date {}", request.getToDate(),
                        request.getFromDate());
                throw new DateMismatchException();
            }

        if (request.getToDate() != null)
            if (request.getToDate().after(new Date())) {
                request.setToDate(new Date());
            }

        if (request.getFromDate() != null && request.getToDate() != null) {
            Calendar fromCal = Calendar.getInstance();
            fromCal.setTime(request.getFromDate());
            Calendar toCal = Calendar.getInstance();
            toCal.setTime(request.getToDate());

            boolean sameDay = fromCal.get(Calendar.YEAR) == toCal.get(Calendar.YEAR) &&
                    fromCal.get(Calendar.DAY_OF_YEAR) == toCal.get(Calendar.DAY_OF_YEAR);

            if (sameDay) {
                fromCal.set(Calendar.HOUR_OF_DAY, 0);
                fromCal.set(Calendar.MINUTE, 0);
                fromCal.set(Calendar.SECOND, 0);
                fromCal.set(Calendar.MILLISECOND, 0);
                request.setFromDate(fromCal.getTime());

                toCal.set(Calendar.HOUR_OF_DAY, 23);
                toCal.set(Calendar.MINUTE, 59);
                toCal.set(Calendar.SECOND, 59);
                toCal.set(Calendar.MILLISECOND, 999);
                request.setToDate(toCal.getTime());
            }
            Duration duration = Duration.between(request.getFromDate().toInstant(), request.getToDate().toInstant());
            log.debug("Duration between dates: {} days", duration.toDays());
            Long daysBetween = duration.toDays();
            if (daysBetween > Long.parseLong(transHistoryDuration)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(request.getToDate());
                cal.add(Calendar.DATE, -Integer.parseInt(transHistoryDuration));
                request.setFromDate(cal.getTime());
            }
        }

        Specification<FinancialTransaction> transactionSpec = new TransactionHistorySpecification(request);
        List<FinancialTransaction> financialTransactions = null;

        try {
            financialTransactions = transactionRepository.findAll(transactionSpec);
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching transactions data");
        }

        File report = null;
        switch (request.getReportType()) {
            case "Daily/Time-Based Report":
                report = generatingDailyTimeBasedReport(financialTransactions);
                break;

            case "Daily Report":
                report = generatingDailyReport(financialTransactions);
                break;

            case "Card Performance Report":
                report = generatingCardPerformanceReport(financialTransactions);
                break;

            case "Customer Transaction Report":
                report = generatingCustomerTransactionReport(financialTransactions);
                break;

            case "Transaction Summary Report":
                report = generatingTransactionSummaryReport(financialTransactions,request.getFromDate(),request.getToDate());
                break;

            case "Payment Processor Report":
                report = generatingPaymentChannelReport(financialTransactions,request.getFromDate(),request.getToDate());
                break;

            case "AlphaPay Products Report":
                report = generateReportByProductType(financialTransactions );
                break;

            case "Session and Retry Behavior":
                report = generateSessionAndRetryBehavior(financialTransactions );
                break;

            case "Invoice and Payment Lifecycle":
                report = generateInvoicePaymentLifecycleReport(financialTransactions);
                break;

            case "Invoice Lifecycle Status Flow":
                report = generateInvoiceLifecycleStatusFlowReport(financialTransactions);
                break;

            default:
                log.warn("Unsupported report type: {}", request.getReportType());
                break;
        }
        // Fallback: throw exception if report is still null (unsupported or missing report type)
        if (report == null) {
            throw new IllegalArgumentException("No report generated for type: " + request.getReportType());
        }
        return report;
    }

    /**
     * Generates an Invoice and Payment Lifecycle report.
     * This report includes invoice ID, merchant name, transaction ID, timestamp, invoice status, and final transaction status for lifecycle tracking.
     */
    private File generateInvoicePaymentLifecycleReport(List<FinancialTransaction> transactions) {
        try {
            File file = tempFile("invoice_payment_lifecycle", ".xlsx");
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Invoice Lifecycle");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Invoice ID");
            header.createCell(1).setCellValue("Merchant Name");
            header.createCell(2).setCellValue("Transaction Type");
            header.createCell(3).setCellValue("Payment ID");
            header.createCell(4).setCellValue("Transaction Time");
            header.createCell(5).setCellValue("Invoice Status");
            header.createCell(6).setCellValue("Transaction Status");

            Map<String, List<FinancialTransaction>> grouped = new LinkedHashMap<>();
            for (FinancialTransaction txn : transactions) {
                if (txn.getInvoice() != null) {
                    String invoiceId = txn.getInvoice().getInvoiceId();
                    grouped.computeIfAbsent(invoiceId, k -> new ArrayList<>()).add(txn);
                }
            }

            int rowIndex = 1;
            for (Map.Entry<String, List<FinancialTransaction>> entry : grouped.entrySet()) {
                for (FinancialTransaction txn : entry.getValue()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(txn.getInvoice().getBusinessName());
                    row.createCell(2).setCellValue(txn.getTransactionType() != null ? txn.getTransactionType() : "N/A");
                    row.createCell(3).setCellValue(txn.getTransactionId() != null ? txn.getPaymentId() : "N/A");
                    row.createCell(4).setCellValue(txn.getTransactionTime() != null ? txn.getCreationTime().toString() : "N/A");
                    row.createCell(5).setCellValue(txn.getInvoiceStatus() != null ? txn.getInvoiceStatus() : "N/A");
                    row.createCell(6).setCellValue(txn.getTransactionStatus() != null ? txn.getTransactionStatus() : "N/A");
                }
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }
            workbook.close();
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Error generating Invoice and Payment Lifecycle report", e);
        }
    }
    private File generateSessionAndRetryBehavior(List<FinancialTransaction> financialTransactions) {
        Map<String, List<FinancialTransaction>> sessionMap = new HashMap<>();
        Map<String, List<FinancialTransaction>> invoiceRetryMap = new HashMap<>();
        Map<String, List<FinancialTransaction>> transactionRetryMap = new HashMap<>();

        for (FinancialTransaction txn : financialTransactions) {
            // Group by session ID
            if (txn.getSessionId() != null) {
                sessionMap.computeIfAbsent(txn.getSessionId(), k -> new ArrayList<>()).add(txn);
            }

            // Group by invoice ID
            if (txn.getInvoice() != null ) {
                String invoiceId = txn.getInvoice().getInvoiceId();
                invoiceRetryMap.computeIfAbsent(invoiceId, k -> new ArrayList<>()).add(txn);
            }

            // Group by transaction number
            if (txn.getTransactionNumber() != null) {
                transactionRetryMap.computeIfAbsent(txn.getTransactionNumber(), k -> new ArrayList<>()).add(txn);
            }
        }

        try {
            File excelFile = tempFile("session_retry_report", ".xlsx");
            Workbook workbook = new XSSFWorkbook();

            Sheet sessionSheet = workbook.createSheet("Session Analysis");
            Sheet invoiceSheet = workbook.createSheet("Invoice Retry");

            int rowIndex = 0;
            Row header = sessionSheet.createRow(rowIndex++);
            header.createCell(0).setCellValue("Session ID");
            header.createCell(1).setCellValue("Transaction Count");
            header.createCell(2).setCellValue("IP Addresses");

            // Create a style with light red (rose) background
            CellStyle lightRedStyle = workbook.createCellStyle();
            lightRedStyle.setFillForegroundColor(IndexedColors.RED1.getIndex());
            lightRedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (Map.Entry<String, List<FinancialTransaction>> entry : sessionMap.entrySet()) {
                Row row = sessionSheet.createRow(rowIndex++);
                Cell sessionIdCell = row.createCell(0);
                sessionIdCell.setCellValue(entry.getKey());

                Cell countCell = row.createCell(1);
                int txnCount = entry.getValue().size();
                countCell.setCellValue(txnCount);

                // Collect distinct IP addresses for the session
                Set<String> ipAddresses = entry.getValue().stream()
                        .map(FinancialTransaction::getIp)
                        .filter(Objects::nonNull)
                        .collect(java.util.stream.Collectors.toSet());
                row.createCell(2).setCellValue(String.join(", ", ipAddresses));

                if (txnCount > 3) {
                    sessionIdCell.setCellStyle(lightRedStyle);
                    countCell.setCellStyle(lightRedStyle);
                    row.getCell(2).setCellStyle(lightRedStyle); // IP cell
                }
            }

            rowIndex = 0;
            header = invoiceSheet.createRow(rowIndex++);
            header.createCell(0).setCellValue("Invoice ID");
            header.createCell(1).setCellValue("Merchant Name");
            header.createCell(2).setCellValue("Retry Count");
            header.createCell(3).setCellValue("Transaction Statuses");

            for (Map.Entry<String, List<FinancialTransaction>> entry : invoiceRetryMap.entrySet()) {
                if (entry.getValue().size() > 1) {
                    Row row = invoiceSheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(entry.getKey());

                    String businessName = entry.getValue().get(0).getInvoice() != null
                            ? entry.getValue().get(0).getInvoice().getBusinessName()
                            : "N/A";
                    row.createCell(1).setCellValue(businessName);

                    row.createCell(2).setCellValue(entry.getValue().size());

                    String statuses = entry.getValue().stream()
                            .map(FinancialTransaction::getTransactionStatus)
                            .filter(Objects::nonNull)
                            .distinct()
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("N/A");
                    row.createCell(3).setCellValue(statuses);
                }
            }


            try (FileOutputStream out = new FileOutputStream(excelFile)) {
                workbook.write(out);
            }
            workbook.close();
            return excelFile;
        } catch (IOException e) {
            throw new RuntimeException("Error generating Session & Retry Behavior report", e);
        }
    }

    private File generateReportByProductType(List<FinancialTransaction> financialTransactions) {
        File excelFile;
        try {
            Map<String, Long> typeCountMap = new LinkedHashMap<>();
            Map<String, BigDecimal> typeAmountMap = new LinkedHashMap<>();
            Map<String, BigDecimal> typeSuccessAmountMap = new LinkedHashMap<>();
            Map<String, BigDecimal> typeFailedAmountMap = new LinkedHashMap<>();

            for (FinancialTransaction transaction : financialTransactions) {
                if (transaction.getInvoice() == null) continue;
                String type = transaction.getInvoice().getType();
                if (type == null) continue;

                BigDecimal amount = transaction.getPaidCurrencyValue() != null ? transaction.getPaidCurrencyValue() : BigDecimal.ZERO;

                typeCountMap.put(type, typeCountMap.getOrDefault(type, 0L) + 1);
                typeAmountMap.put(type, typeAmountMap.getOrDefault(type, BigDecimal.ZERO).add(amount));

                if ("SUCCESS".equalsIgnoreCase(transaction.getTransactionStatus())) {
                    typeSuccessAmountMap.put(type, typeSuccessAmountMap.getOrDefault(type, BigDecimal.ZERO).add(amount));
                } else {
                    typeFailedAmountMap.put(type, typeFailedAmountMap.getOrDefault(type, BigDecimal.ZERO).add(amount));
                }
            }

            excelFile = tempFile("payment_type_report", ".xlsx");
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Payment Type Report");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Product Type");
            header.createCell(1).setCellValue("Transaction Count");
            header.createCell(2).setCellValue("Total Amount");
            header.createCell(3).setCellValue("Success Amount");
            header.createCell(4).setCellValue("Failed Amount");

            int rowIndex = 1;
            for (String type : List.of("STANDARD", "INVOICE", "STATIC_QR", "PAYMENT_GATEWAY", "DIRECT_PAYMENT")) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(type);
                row.createCell(1).setCellValue(typeCountMap.getOrDefault(type, 0L));
                row.createCell(2).setCellValue(typeAmountMap.getOrDefault(type, BigDecimal.ZERO).doubleValue());
                row.createCell(3).setCellValue(typeSuccessAmountMap.getOrDefault(type, BigDecimal.ZERO).doubleValue());
                row.createCell(4).setCellValue(typeFailedAmountMap.getOrDefault(type, BigDecimal.ZERO).doubleValue());
            }

            try (FileOutputStream outputStream = new FileOutputStream(excelFile)) {
                workbook.write(outputStream);
            }
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating Product Type Report", e);
        }
        return excelFile;
    }

    private File generatingPaymentChannelReport(List<FinancialTransaction> financialTransactions, Date fromDate, Date toDate) {
        File excelFile;
        try {
            int myFatoorahCount = 0;
            int mbmeCount = 0;

            BigDecimal myFatoorahAmount = BigDecimal.ZERO;
            BigDecimal mbmeAmount = BigDecimal.ZERO;

            // Add maps for failed messages
            Map<String, Long> myFatoorahFailedMessages = new HashMap<>();
            Map<String, Long> mbmeFailedMessages = new HashMap<>();

            for (FinancialTransaction transaction : financialTransactions) {
                Long processorId = transaction.getProcessorId();
                BigDecimal amount = transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO;

                boolean isSuccess = "SUCCESS".equalsIgnoreCase(transaction.getTransactionStatus());
                String responseMessage = transaction.getResponseMessage();

                if ((processorId == null || processorId == 1L)) {
                    myFatoorahCount++;
                    myFatoorahAmount = myFatoorahAmount.add(amount);
                    if (!isSuccess && responseMessage != null) {
                        myFatoorahFailedMessages.put(responseMessage, myFatoorahFailedMessages.getOrDefault(responseMessage, 0L) + 1);
                    }
                } else if (processorId == 2L) {
                    mbmeCount++;
                    mbmeAmount = mbmeAmount.add(amount);
                    if (!isSuccess && responseMessage != null) {
                        mbmeFailedMessages.put(responseMessage, mbmeFailedMessages.getOrDefault(responseMessage, 0L) + 1);
                    }
                }
            }

            excelFile = tempFile("payment_channel_report", ".xlsx");
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Payment Channel Report");

            Row headerDate = sheet.createRow(0);
            headerDate.createCell(0).setCellValue("From");
            headerDate.createCell(1).setCellValue(fromDate.toString());
            headerDate.createCell(2).setCellValue("To");
            headerDate.createCell(3).setCellValue(toDate.toString());

            Row header = sheet.createRow(2);
            header.createCell(0).setCellValue("Payment Channel");
            header.createCell(1).setCellValue("Transaction Count");
            header.createCell(2).setCellValue("Total Amount");

            Row myFatoorahRow = sheet.createRow(3);
            myFatoorahRow.createCell(0).setCellValue("MyFatoorah");
            myFatoorahRow.createCell(1).setCellValue(myFatoorahCount);
            myFatoorahRow.createCell(2).setCellValue(myFatoorahAmount.doubleValue());

            Row mbmeRow = sheet.createRow(4);
            mbmeRow.createCell(0).setCellValue("MBME");
            mbmeRow.createCell(1).setCellValue(mbmeCount);
            mbmeRow.createCell(2).setCellValue(mbmeAmount.doubleValue());

            // Add failed messages table after MBME row
            int rowNum = 6;
            Row messageHeader = sheet.createRow(rowNum++);
            messageHeader.createCell(0).setCellValue("Channel");
            messageHeader.createCell(1).setCellValue("Failure Message");
            messageHeader.createCell(2).setCellValue("Count");

            for (Map.Entry<String, Long> entry : myFatoorahFailedMessages.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("MyFatoorah");
                row.createCell(1).setCellValue(entry.getKey());
                row.createCell(2).setCellValue(entry.getValue());
            }

            for (Map.Entry<String, Long> entry : mbmeFailedMessages.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("MBME");
                row.createCell(1).setCellValue(entry.getKey());
                row.createCell(2).setCellValue(entry.getValue());
            }

            try (FileOutputStream outputStream = new FileOutputStream(excelFile)) {
                workbook.write(outputStream);
            }
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating Payment Channel Report", e);
        }
        return excelFile;
    }

    private File generatingTransactionSummaryReport(List<FinancialTransaction> financialTransactions, Date fromDate, Date toDate) {
        File excelFile;
        try {
            int totalTransactions = financialTransactions.size();
            int successCount = 0;
            int failedCount = 0;
            BigDecimal totalSuccessAmountAED = new BigDecimal("0.0");
            BigDecimal totalFailedAmountAED = new BigDecimal("0.0");

            for (FinancialTransaction transaction : financialTransactions) {

                boolean isSuccess = "SUCCESS".equalsIgnoreCase(transaction.getTransactionStatus());
                if (isSuccess) {
                    if (transaction.getPaidCurrencyValue() != null) {
                        totalSuccessAmountAED = totalSuccessAmountAED.add(transaction.getPaidCurrencyValue());
                    }
                    successCount++;
                } else {
                    if (transaction.getPaidCurrencyValue() != null) {
                        totalFailedAmountAED = totalFailedAmountAED.add(transaction.getPaidCurrencyValue());
                    }
                    failedCount++;
                }

            }

            double successRate = totalTransactions > 0 ? (successCount * 100.0 / totalTransactions) : 0.0;

            excelFile = tempFile("transaction_summary"+fromDate.toString()+"-"+toDate.toString(), ".xlsx");
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Summary");

            // --- Cell styles for formatting ---
            // Create cell styles
            org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            org.apache.poi.ss.usermodel.CellStyle successStyle = workbook.createCellStyle();
            successStyle.cloneStyleFrom(boldStyle);
            successStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            successStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            org.apache.poi.ss.usermodel.CellStyle failedStyle = workbook.createCellStyle();
            failedStyle.cloneStyleFrom(boldStyle);
            failedStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            failedStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            // --- End cell styles ---

            Row headerRow = sheet.createRow(0);
            Cell headerCell0 = headerRow.createCell(0);
            headerCell0.setCellValue("Metric");
            headerCell0.setCellStyle(boldStyle);
            Cell headerCell1 = headerRow.createCell(1);
            headerCell1.setCellValue("Value");
            headerCell1.setCellStyle(boldStyle);

            Object[][] summaryData = {
                {"From Date", fromDate != null ? fromDate.toString() : "N/A"},
                {"To Date", toDate != null ? toDate.toString() : "N/A"},
                {"Total Transactions", totalTransactions},
                {"Successful Transactions", successCount},
                {"Failed Transactions", failedCount},
                {"Total Successful Amount (AED)", totalSuccessAmountAED},
                {"Total Failed Amount (AED)", totalFailedAmountAED},
                {"Success Rate (%)", successRate}
            };

            for (int i = 0; i < summaryData.length; i++) {
                Row row = sheet.createRow(i + 1);
                // Apply bold to metric label, and color rows for Success/Failed
                Cell labelCell = row.createCell(0);
                labelCell.setCellValue(summaryData[i][0].toString());
                labelCell.setCellStyle(boldStyle);

                Cell valueCell = row.createCell(1);
                if (summaryData[i][1] instanceof Number) {
                    valueCell.setCellValue(Double.parseDouble(summaryData[i][1].toString()));
                } else {
                    valueCell.setCellValue(summaryData[i][1].toString());
                }

                String label = summaryData[i][0].toString();
                if (label.contains("Successful")) {
                    valueCell.setCellStyle(successStyle);
                } else if (label.contains("Failed")) {
                    valueCell.setCellStyle(failedStyle);
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream(excelFile)) {
                workbook.write(outputStream);
            }
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating Transaction Summary Report", e);
        }

        return excelFile;
    }


    /**
     * Generates an Invoice Lifecycle Status Flow report.
     * This report includes invoice ID, merchant name, payment ID, transaction time, invoice status history, and final transaction status.
     */
    private File generateInvoiceLifecycleStatusFlowReport(List<FinancialTransaction> transactions) {
        try {
            File file = tempFile("invoice_lifecycle_status_flow", ".xlsx");
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Lifecycle Status Flow");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Invoice ID");
            header.createCell(1).setCellValue("Merchant Name");
            header.createCell(2).setCellValue("Transaction Type");
            header.createCell(3).setCellValue("Payment ID");
            header.createCell(4).setCellValue("Transaction Time");
            header.createCell(5).setCellValue("Invoice Status History");
            header.createCell(6).setCellValue("Final Transaction Status");


            Map<String, List<FinancialTransaction>> grouped = new LinkedHashMap<>();
            for (FinancialTransaction txn : transactions) {
                if (txn.getInvoice() != null) {
                    String invoiceId = txn.getInvoice().getInvoiceId();
                    grouped.computeIfAbsent(invoiceId, k -> new ArrayList<>()).add(txn);
                }
            }

            int rowIndex = 1;
            for (Map.Entry<String, List<FinancialTransaction>> entry : grouped.entrySet()) {
                List<FinancialTransaction> txns = entry.getValue();
                txns.sort(Comparator.comparing(FinancialTransaction::getCreationTime)); // Sort by creation time

                String statusHistory = txns.stream()
                        .map(FinancialTransaction::getInvoiceStatus)
                        .filter(Objects::nonNull)
                        .distinct()
                        .reduce((a, b) -> a + " ➝ " + b)
                        .orElse("N/A");

                String finalStatus = txns.get(txns.size() - 1).getTransactionStatus();

                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(txns.get(0).getInvoice().getBusinessName());
                row.createCell(2).setCellValue(txns.get(0).getTransactionType() != null ? txns.get(0).getTransactionType() : "N/A");
                row.createCell(3).setCellValue(txns.get(0).getPaymentId());
                row.createCell(4).setCellValue(txns.get(0).getTransactionTime() != null ? txns.get(0).getTransactionTime().toString() : "N/A");
                row.createCell(5).setCellValue(statusHistory);
                row.createCell(6).setCellValue(finalStatus != null ? finalStatus : "N/A");
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }
            workbook.close();
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Error generating Invoice Lifecycle Status Flow report", e);
        }
    }

    private File generatingCustomerTransactionReport(List<FinancialTransaction> financialTransactions) {
        return null;
    }

    private File generatingCardPerformanceReport(List<FinancialTransaction> financialTransactions) {
        Map<String, CardStat> cardStats = new java.util.HashMap<>();

        for (FinancialTransaction transaction : financialTransactions) {
            String card = transaction.getCardNumber();
            if (card == null || card.length() < 10) continue;

            String maskedCard = card.substring(0, 6) + "******" + card.substring(card.length() - 4);
            CardStat stat = cardStats.getOrDefault(maskedCard, new CardStat());
            CustomerCardBINInfoResponse binData = binService.getBinInfo(card.substring(0, 6));
            log.debug("binData-------->{}", binData);
            String brand = binData.getBrand() != null && !binData.getBrand().isEmpty() ? binData.getBrand() : "N/A";
            String type = binData.getType() != null && !binData.getType().isEmpty() ? binData.getType() : "N/A";
            String category = binData.getCategory() != null && !binData.getCategory().isEmpty() ? binData.getCategory() : "N/A";
            String issuer = binData.getIssuer() != null && !binData.getIssuer().isEmpty() ? binData.getIssuer() : "N/A";
            String countryName = binData.getCountryName() != null && !binData.getCountryName().isEmpty() ? binData.getCountryName() : "N/A";
            if ("SUCCESS".equalsIgnoreCase(transaction.getTransactionStatus())) {
                if (transaction.getAmount() != null) {
                    stat.successTotalAmount = stat.successTotalAmount.add(transaction.getAmount());
                }
                stat.successCount++;
                stat.brand = brand;
                stat.type = type;
                stat.category = category;
                stat.issuer = issuer;
                stat.countryName = countryName;
            } else {
                if (transaction.getAmount() != null) {
                    stat.failureTotalAmount = stat.failureTotalAmount.add(transaction.getAmount());
                }
                stat.failureCount++;
                stat.brand = brand;
                stat.type = type;
                stat.category = category;
                stat.issuer = issuer;
                stat.countryName = countryName;
            }
            cardStats.put(maskedCard, stat);
        }

        File excelFile;
        try {
            excelFile = tempFile("card_performance", ".xlsx");
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Card Performance");
            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Masked Card Number");
            header.createCell(1).setCellValue("brand");
            header.createCell(2).setCellValue("type");
            header.createCell(3).setCellValue("category");
            header.createCell(4).setCellValue("issuer");
            header.createCell(5).setCellValue("countryName");
            header.createCell(6).setCellValue("Success Count");
            header.createCell(7).setCellValue("Success Total Amount");
            header.createCell(8).setCellValue("Failure Count");
            header.createCell(9).setCellValue("Failure Total Amount");

            int rowIndex = 1;
            for (java.util.Map.Entry<String, CardStat> entry : cardStats.entrySet()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue().brand);
                row.createCell(2).setCellValue(entry.getValue().type);
                row.createCell(3).setCellValue(entry.getValue().category);
                row.createCell(4).setCellValue(entry.getValue().issuer);
                row.createCell(5).setCellValue(entry.getValue().countryName);
                row.createCell(6).setCellValue(entry.getValue().successCount);
                row.createCell(7).setCellValue(entry.getValue().successTotalAmount.doubleValue());
                row.createCell(8).setCellValue(entry.getValue().failureCount);
                row.createCell(9).setCellValue(entry.getValue().failureTotalAmount.doubleValue());
            }

            try (FileOutputStream outputStream = new FileOutputStream(excelFile)) {
                workbook.write(outputStream);
            }
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating Card Performance Report", e);
        }
        return excelFile;
    }

    private File generatingDailyTimeBasedReport(List<FinancialTransaction> financialTransactions) throws IOException {

        File excelFile = tempFile("transactions", ".xlsx");
        List<String> columns = List.of(
                "creationTime",
                "transactionTime",
                "transactionType",
                "MerchantId",
                "BusinessName",
                "currency",
                "amount",
                "paidCurrency",
                "paidCurrencyValue",
                "transactionStatus",
                "paymentId",
                "externalPaymentId",
                "invoiceLink",
                "invoiceStatus",
                "paymentMethod",
                "processor");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions");
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns.get(i));
        }

        for (int i = 0; i < financialTransactions.size(); i++) {
            Row row = sheet.createRow(i + 1);
            FinancialTransaction transaction = financialTransactions.get(i);

            for (int j = 0; j < columns.size(); j++) {
                Cell cell = row.createCell(j);
                switch (columns.get(j)) {
                    case "transactionType" ->
                            cell.setCellValue(transaction.getTransactionType() != null ? (transaction.getTransactionType().equals("ExecutePaymentRequest") ? "PAYMENT" : "REFUND") : "N/A");
                    case "MerchantId" ->
                            cell.setCellValue(transaction.getInvoice().getMerchantUserAccount() != null ? transaction.getInvoice().getMerchantUserAccount().getId().toString() : "N/A");
                    case "BusinessName" ->
                            cell.setCellValue(transaction.getInvoice() != null ? transaction.getInvoice().getBusinessName() : "N/A");
                    case "transactionId" ->
                            cell.setCellValue(transaction.getTransactionId() != null ? transaction.getTransactionId() : "N/A");
                    case "transactionNumber" ->
                            cell.setCellValue(transaction.getTransactionNumber() != null ? transaction.getTransactionNumber() : "N/A");
                    case "customerReference" ->
                            cell.setCellValue(transaction.getCustomerReference() != null ? transaction.getCustomerReference() : "N/A");
                    case "comments" ->
                            cell.setCellValue(transaction.getComments() != null ? transaction.getComments() : "N/A");
                    case "currency" ->
                            cell.setCellValue(transaction.getCurrency() != null ? transaction.getCurrency() : "N/A");
                    case "transactionStatus" ->
                            cell.setCellValue(transaction.getTransactionStatus() != null ? transaction.getTransactionStatus() : "N/A");
                    case "paymentId" ->
                            cell.setCellValue(transaction.getPaymentId() != null ? transaction.getPaymentId() : "N/A");
                    case "invoiceLink" ->
                            cell.setCellValue(transaction.getInvoiceLink() != null ? transaction.getInvoiceLink() : "N/A");
                    case "externalPaymentId" ->
                            cell.setCellValue(transaction.getExternalPaymentId() != null ? transaction.getExternalPaymentId() : "N/A");
                    case "invoiceStatus" ->
                            cell.setCellValue(transaction.getInvoiceStatus() != null ? transaction.getInvoiceStatus() : "N/A");
                    case "transactionTime" ->
                            cell.setCellValue(transaction.getTransactionTime() != null ? transaction.getTransactionTime().toString() : "N/A");
                    case "depositShare" ->
                            cell.setCellValue(transaction.getDepositShare() != null ? transaction.getDepositShare().doubleValue() : 0.0);
                    case "totalCharges" ->
                            cell.setCellValue(transaction.getTotalCharges() != null ? transaction.getTotalCharges().doubleValue() : 0.0);
                    case "vat" ->
                            cell.setCellValue(transaction.getVat() != null ? transaction.getVat().doubleValue() : 0.0);
                    case "amount" ->
                            cell.setCellValue(transaction.getAmount() != null ? transaction.getAmount().doubleValue() : 0.0);
                    case "paidCurrency" ->
                            cell.setCellValue(transaction.getPaidCurrency() != null ? transaction.getPaidCurrency() : "N/A");
                    case "paidCurrencyValue" ->
                            cell.setCellValue(transaction.getPaidCurrencyValue() != null ? transaction.getPaidCurrencyValue().doubleValue() : 0.0);
                    case "paymentMethod" ->
                            cell.setCellValue(transaction.getPaymentMethod() != null ? transaction.getPaymentMethod() : "N/A");
                    case "creationTime" ->
                            cell.setCellValue(transaction.getCreationTime() != null ? transaction.getCreationTime().toString() : "N/A");
                    case "customerServiceCharge" ->
                            cell.setCellValue(transaction.getCustomerServiceCharge() != null ? transaction.getCustomerServiceCharge().doubleValue() : 0.0);
                    case "processor" ->
                            cell.setCellValue(transaction.getProcessorId() != null ? (transaction.getProcessorId() == 1 ? "MF" : "MBME") : "N/A");
                    default -> cell.setCellValue("N/A");
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(excelFile)) {
            workbook.write(outputStream);
        }
        workbook.close();

        return excelFile;
    }

    private File generatingDailyReport(List<FinancialTransaction> financialTransactions) throws IOException {

        File excelFile = tempFile("transactions", ".xlsx");
        List<String> columns = List.of(
                "creationTime",
                "transactionType",
                "MerchantId",
                "BusinessName",
                "amount",
                "currency",
                "paidCurrencyValue",
                "paidCurrency",
                "invoiceStatus",
                "transactionStatus",
                "paymentId",
                "externalPaymentId",
                "processor");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions");
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns.get(i));
        }

        for (int i = 0; i < financialTransactions.size(); i++) {
            Row row = sheet.createRow(i + 1);
            FinancialTransaction transaction = financialTransactions.get(i);

            for (int j = 0; j < columns.size(); j++) {
                Cell cell = row.createCell(j);
                switch (columns.get(j)) {
                    case "transactionType" ->
                            cell.setCellValue(transaction.getTransactionType() != null ? (transaction.getTransactionType().equals("ExecutePaymentRequest") ? "PAYMENT" : "REFUND") : "N/A");
                    case "MerchantId" ->
                            cell.setCellValue(transaction.getInvoice().getMerchantUserAccount() != null ? transaction.getInvoice().getMerchantUserAccount().getId().toString() : "N/A");
                    case "BusinessName" ->
                            cell.setCellValue(transaction.getInvoice() != null ? transaction.getInvoice().getBusinessName() : "N/A");
                    case "transactionId" ->
                            cell.setCellValue(transaction.getTransactionId() != null ? transaction.getTransactionId() : "N/A");
                    case "transactionNumber" ->
                            cell.setCellValue(transaction.getTransactionNumber() != null ? transaction.getTransactionNumber() : "N/A");
                    case "customerReference" ->
                            cell.setCellValue(transaction.getCustomerReference() != null ? transaction.getCustomerReference() : "N/A");
                    case "comments" ->
                            cell.setCellValue(transaction.getComments() != null ? transaction.getComments() : "N/A");
                    case "currency" ->
                            cell.setCellValue(transaction.getCurrency() != null ? transaction.getCurrency() : "N/A");
                    case "transactionStatus" ->
                            cell.setCellValue(transaction.getTransactionStatus() != null ? transaction.getTransactionStatus() : "N/A");
                    case "invoiceStatus" ->
                            cell.setCellValue(transaction.getInvoiceStatus() != null ? transaction.getInvoiceStatus() : "N/A");
                    case "paymentId" ->
                            cell.setCellValue(transaction.getPaymentId() != null ? transaction.getPaymentId() : "N/A");
                     case "externalPaymentId" ->
                            cell.setCellValue(transaction.getExternalPaymentId() != null ? transaction.getExternalPaymentId() : "N/A");
                    case "depositShare" ->
                            cell.setCellValue(transaction.getDepositShare() != null ? transaction.getDepositShare().doubleValue() : 0.0);
                    case "totalCharges" ->
                            cell.setCellValue(transaction.getTotalCharges() != null ? transaction.getTotalCharges().doubleValue() : 0.0);
                    case "vat" ->
                            cell.setCellValue(transaction.getVat() != null ? transaction.getVat().doubleValue() : 0.0);
                    case "amount" ->
                            cell.setCellValue(transaction.getAmount() != null ? transaction.getAmount().doubleValue() : 0.0);
                    case "paidCurrency" ->
                            cell.setCellValue(transaction.getPaidCurrency() != null ? transaction.getPaidCurrency() : "N/A");
                    case "paidCurrencyValue" ->
                            cell.setCellValue(transaction.getPaidCurrencyValue() != null ? transaction.getPaidCurrencyValue().doubleValue() : 0.0);
                    case "paymentMethod" ->
                            cell.setCellValue(transaction.getPaymentMethod() != null ? transaction.getPaymentMethod() : "N/A");
                    case "creationTime" ->
                            cell.setCellValue(transaction.getCreationTime() != null ? transaction.getCreationTime().toString() : "N/A");
                    case "customerServiceCharge" ->
                            cell.setCellValue(transaction.getCustomerServiceCharge() != null ? transaction.getCustomerServiceCharge().doubleValue() : 0.0);
                    case "processor" ->
                            cell.setCellValue(transaction.getProcessorId() != null ? (transaction.getProcessorId() == 1 ? "MF" : "MBME") : "N/A");
                    default -> cell.setCellValue("N/A");
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(excelFile)) {
            workbook.write(outputStream);
        }
        workbook.close();

        return excelFile;
    }

    /**
     * CardStat class for holding statistics per card.
     */
    private static class CardStat {
        int successCount = 0;
        int failureCount = 0;
        String brand = "N/A";
        String type = "N/A";
        String category = "N/A";
        String issuer = "N/A";
        String countryName = "N/A";
        BigDecimal successTotalAmount = BigDecimal.ZERO;
        BigDecimal failureTotalAmount = BigDecimal.ZERO;
    }

    private static File tempFile(String prefix, String suffix) throws IOException {
        Path dir = Path.of(System.getenv().getOrDefault("WRITABLE_TMP", "/app/tmp"));
        log.debug("Temp path is {}", dir);
        return Files.createTempFile(dir, prefix, suffix).toFile();
    }
}
