package com.alphapay.payEngine.alphaServices.historyTransaction.serviceImpl;

import com.alphapay.payEngine.account.management.dto.response.PaginatedResponse;
import com.alphapay.payEngine.account.management.exception.DateMismatchException;
import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.account.roles.exception.UserNotFoundException;
import com.alphapay.payEngine.alphaServices.dto.response.MerchantStats;
import com.alphapay.payEngine.alphaServices.historyTransaction.dto.request.TransactionHistoryRequest;
import com.alphapay.payEngine.alphaServices.historyTransaction.dto.response.TransactionStats;
import com.alphapay.payEngine.alphaServices.historyTransaction.service.TransHistoryService;
import com.alphapay.payEngine.alphaServices.service.MerchantAlphaPayServicesService;
import com.alphapay.payEngine.alphaServices.serviceImpl.AiRecommendationService;
import com.alphapay.payEngine.notification.services.INotificationService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import com.alphapay.payEngine.utilities.PaymentStepsType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransHistoryServiceImpl implements TransHistoryService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Autowired
    UserRepository userRepository;
    @Autowired
    private FinancialTransactionRepository transactionRepository;
    @Value("${trans.history.page.size}")
    private String historyPageSize;
    @Value("${trans.history.duration}")
    private String transHistoryDuration;
    @Value("${summary.report.time}")
    private String summaryReportsTime;
    @Autowired
    private MerchantAlphaPayServicesService merchantService;
    @Autowired
    private AiRecommendationService aiService;
    @Autowired
    private INotificationService notificationService;

    private static String toJson(Map<?, ?> map) {
        try {
            return OBJECT_MAPPER.writeValueAsString(map == null ? Collections.emptyMap() : map);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String fmt(double v) {
        // Avoid scientific notation and strip trailing zeros where possible
        return new java.math.BigDecimal(Double.toString(v)).stripTrailingZeros().toPlainString();
    }

    @Scheduled(cron = "${summary.report.time}")
    public void sendDailyReport() throws Exception {
        TransactionStats txStats = getLast24hStats();
        MerchantStats merchantStats = merchantService.getLast24hStats();

        String recommendations = aiService.generateInsights(txStats, merchantStats).replace("#", "").replace("*", "");

        String[] msgKeys = {
                String.valueOf(txStats.getTotalCount()),
                fmt(txStats.getTotalAmount()),
                String.valueOf(txStats.getSuccessCount()),
                fmt(txStats.getSuccessAmount()),
                String.valueOf(txStats.getFailCount()),
                fmt(txStats.getFailAmount()),
                String.valueOf(txStats.getPendingCount()),
                String.valueOf(txStats.getInProgressCount()),
                toJson(txStats.getFailureReasons()),

                String.valueOf(merchantStats.getNewMerchantsToday()),
                String.valueOf(merchantStats.getApprovedToday()),
                String.valueOf(merchantStats.getLastLoginCount()),
                String.valueOf(merchantStats.getRejectedToday()),
                fmt(merchantStats.getAvgApprovalHours()),
                toJson(merchantStats.getManagerApprovalBreakdown()),
                toJson(merchantStats.getAdminApprovalBreakdown()),
                toJson(merchantStats.getMbmeApprovalBreakdown()),
                toJson(merchantStats.getMyfattoraApprovalBreakdown()),
                String.valueOf(merchantStats.getLockedCount()),
                String.valueOf(merchantStats.getDisabledCount()),
                recommendations,
                String.valueOf(txStats.getMinCommission()),
                String.valueOf(txStats.getMaxCommission())
        };
        byte[] pdfBytes = DailyPaymentMerchantReportPdf.build(msgKeys);
log.debug("<----------------SendingReportByEmail---------------->");
        notificationService.sendEmailNotification(UUID.randomUUID().toString(), "DAILY_PAYMENT_MERCHANT_REPORT", msgKeys, "m.alshayib@alphapay.ae", "", Locale.ENGLISH, "004789", "Daily Payment Merchant Report", pdfBytes);
    }

    @Override
    public PaginatedResponse<FinancialTransaction> getAllExecutePaymentTransaction(TransactionHistoryRequest request) {
        log.debug("TransactionHistoryRequest:::::{}", request);
        if (request.getToDate() != null) if (request.getToDate().before(request.getFromDate())) {
            log.debug("Date mismatch: To date {} is before From date {}", request.getToDate(), request.getFromDate());
            throw new DateMismatchException();
        }

        if (request.getToDate() != null) if (request.getToDate().after(new Date())) {
            request.setToDate(new Date());
        }

        List<Long> subsMerchants = new ArrayList<>();
        if (request.getMerchantId() != null && request.getMerchantId() > 0) {
            UserEntity superMerchant = userRepository.findById(request.getMerchantId()).orElse(null);
            if (superMerchant != null && superMerchant.getSubUsers() != null) {
                subsMerchants = superMerchant.getSubUsers().stream()
                        .map(UserEntity::getId)
                        .collect(Collectors.toList());
            }
        }
        if (request.getSubMerchantId() != null && !subsMerchants.contains(request.getSubMerchantId())) {
            throw new UserNotFoundException();
        }
        request.setSubMerchantIds(subsMerchants);

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
        int pageIndex = request.getPageNumber() == null ? 0 : request.getPageNumber() - 1;
        int pageSize = request.getPageSize() == null ? Integer.parseInt(historyPageSize) : request.getPageSize();
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("creationTime").descending());
        Page<FinancialTransaction> financialTransactions;
        try {
            financialTransactions = transactionRepository.findAll(transactionSpec, pageRequest);
        } catch (Exception ex) {
            log.error("Error fetching payment link data", ex);
            throw new RuntimeException("Error fetching payment link data");
        }

        List<FinancialTransaction> responseList = financialTransactions.stream().map(transaction -> {
            FinancialTransaction customerInfo = transactionRepository.findFirstByPaymentIdAndTransactionTypeAndHttpResponseCodeOrderByLastUpdatedDesc(transaction.getPaymentId(), PaymentStepsType.INITIATE_PAYMENT_S2.getName(), "200").orElse(null);
            FinancialTransaction transHistory = new FinancialTransaction();
            BeanUtils.copyProperties(transaction, transHistory);

            if (customerInfo != null) {
                transHistory.setCustomerInfo(customerInfo.getIncomingPaymentAttributes());
            }
            return transHistory;
        }).collect(Collectors.toList());

        return new PaginatedResponse<>(responseList, financialTransactions.getNumber() + 1, // Convert zero-based page index to one-based
                financialTransactions.getSize(), financialTransactions.getTotalElements(), financialTransactions.getTotalPages(), financialTransactions.isLast());

    }


    @Override
    public List<Map<String, Object>> getTransactionSummary(TransactionHistoryRequest request) {

        if (request.getToDate() != null) if (request.getToDate().before(request.getFromDate())) {
            log.debug("Date mismatch: To date {} is before From date {}", request.getToDate(), request.getFromDate());
            throw new DateMismatchException();
        }

        if (request.getToDate() != null) if (request.getToDate().after(new Date())) {
            request.setToDate(new Date());
        }

        if (request.getFromDate() != null && request.getToDate() != null) {
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
        List<Long> subsMerchants = new ArrayList<>();
        if (request.getMerchantId() != null && request.getMerchantId() > 0) {
            UserEntity superMerchant = userRepository.findById(request.getMerchantId()).orElse(null);
            if (superMerchant != null && superMerchant.getSubUsers() != null) {
                subsMerchants = superMerchant.getSubUsers().stream()
                        .map(UserEntity::getId)
                        .collect(Collectors.toList());
            }
        }
        request.setSubMerchantIds(subsMerchants);


        Specification<FinancialTransaction> transactionSpec = new TransactionHistorySpecification(request);
        List<FinancialTransaction> financialTransactions;
        try {
            financialTransactions = transactionRepository.findAll(transactionSpec);
        } catch (Exception ex) {
            log.error("Error fetching payment link data", ex);
            throw new RuntimeException("Error fetching payment link data");
        }
        return generateGroupedResponse(financialTransactions);
    }


    public List<Map<String, Object>> generateGroupedResponse(List<FinancialTransaction> transactions) {
        Map<LocalDate, List<FinancialTransaction>> transactionsByDate = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCreationTime().toLocalDate()));

        List<Map<String, Object>> response = new ArrayList<>();
        transactionsByDate.forEach((date, transactionsOnDate) -> {
            Map<String, Object> dateEntry = new HashMap<>();
            dateEntry.put("date", date.toString());

            Map<String, Object> successful = new HashMap<>();
            List<FinancialTransaction> successfulTransactions = transactionsOnDate.stream()
                    .filter(t -> "Succss".equalsIgnoreCase(t.getTransactionStatus()) || "Success".equalsIgnoreCase(t.getTransactionStatus()))
                    .toList();
            successful.put("count", successfulTransactions.size());
            successful.put("sumOfAmount", successfulTransactions.stream()
                    .map(FinancialTransaction::getPaidCurrencyValue)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            Map<String, Object> inProgress = new HashMap<>();
            List<FinancialTransaction> inProgressTransactions = transactionsOnDate.stream()
                    .filter(t -> "InProgress".equalsIgnoreCase(t.getTransactionStatus()))
                    .collect(Collectors.toList());
            inProgress.put("count", inProgressTransactions.size());
            inProgress.put("sumOfAmount", inProgressTransactions.stream()
                    .map(FinancialTransaction::getPaidCurrencyValue)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            Map<String, Object> failed = new HashMap<>();
            List<FinancialTransaction> failedTransactions = transactionsOnDate.stream()
                    .filter(t -> "Failed".equalsIgnoreCase(t.getTransactionStatus()))
                    .toList();
            failed.put("count", failedTransactions.size());
            failed.put("sumOfAmount", failedTransactions.stream()
                    .map(FinancialTransaction::getPaidCurrencyValue)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            Map<String, Object> unknown = new HashMap<>();
            List<FinancialTransaction> unknownTransactions = transactionsOnDate.stream()
                    .filter(t -> "Cancelled".equalsIgnoreCase(t.getTransactionStatus()))
                    .toList();
            unknown.put("count", unknownTransactions.size());
            unknown.put("sumOfAmount", unknownTransactions.stream()
                    .map(FinancialTransaction::getPaidCurrencyValue)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            dateEntry.put("successful", successful);
            dateEntry.put("inProgress", inProgress);
            dateEntry.put("failed", failed);
            dateEntry.put("cancelled", unknown);

            response.add(dateEntry);
        });

        // Add last five transactions
        List<FinancialTransaction> lastFiveTransactions = transactions.stream()
                .sorted(Comparator.comparing(FinancialTransaction::getCreationTime).reversed())
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> lastFiveMap = new HashMap<>();
        lastFiveMap.put("lastFiveTransactions", lastFiveTransactions);
        response.add(lastFiveMap);

        return response;
    }

    public TransactionStats getLast24hStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusHours(24);

        List<FinancialTransaction> transactions = transactionRepository.findByCreationTimeBetweenAndTransactionType(yesterday, now, PaymentStepsType.EXECUTE_PAYMENT_S3.getName());

        int totalCount = transactions.size();
        BigDecimal totalAmount = transactions.stream()
                .map(FinancialTransaction::getPaidCurrencyValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<FinancialTransaction> successTx = transactions.stream().filter(t -> "Success".equalsIgnoreCase(t.getTransactionStatus())).toList();
        List<FinancialTransaction> failTx = transactions.stream().filter(t -> "Failed".equalsIgnoreCase(t.getTransactionStatus())).toList();
        List<FinancialTransaction> pendingTx = transactions.stream().filter(t -> "Cancelled".equalsIgnoreCase(t.getTransactionStatus())).toList();
        List<FinancialTransaction> inProgressTx = transactions.stream().filter(t -> "InProgress".equalsIgnoreCase(t.getTransactionStatus())).toList();

        BigDecimal successAmount = successTx.stream()
                .map(FinancialTransaction::getPaidCurrencyValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal failAmount = failTx.stream()
                .map(FinancialTransaction::getPaidCurrencyValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Integer> failureReasons = failTx.stream()
                .collect(Collectors.groupingBy(FinancialTransaction::getResponseMessage, Collectors.summingInt(e -> 1)));
        double totalFixedCommission = successTx.size();
        double minCommission = BigDecimal.valueOf(successAmount.doubleValue() * 0.009 + totalFixedCommission).setScale(2, RoundingMode.CEILING)
                .doubleValue();
        double maxCommission = BigDecimal.valueOf(successAmount.doubleValue() * 0.015 + totalFixedCommission)
                .setScale(2, RoundingMode.CEILING)
                .doubleValue();
        return new TransactionStats(
                totalCount,
                totalAmount.doubleValue(),
                successTx.size(),
                successAmount.doubleValue(),
                failTx.size(),
                failAmount.doubleValue(),
                pendingTx.size(),
                inProgressTx.size(),
                failureReasons,
                minCommission,
                maxCommission
        );
    }
}
