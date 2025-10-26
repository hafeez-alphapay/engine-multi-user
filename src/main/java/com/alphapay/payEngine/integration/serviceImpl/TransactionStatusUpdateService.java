package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.account.management.repository.UserRepository;
import com.alphapay.payEngine.alphaServices.dto.response.TransactionStatusResponse;
import com.alphapay.payEngine.alphaServices.model.PaymentLinkEntity;
import com.alphapay.payEngine.alphaServices.repository.PaymentLinkEntityRepository;
import com.alphapay.payEngine.config.JacksonConfig;
import com.alphapay.payEngine.config.MyFatoorahConfig;
import com.alphapay.payEngine.integration.dto.request.PaymentStatusRequest;
import com.alphapay.payEngine.integration.dto.response.paymentStatus.PaymentStatusResponse;
import com.alphapay.payEngine.integration.model.BackEndResponseCodeMapping;
import com.alphapay.payEngine.integration.repository.BackEndResponseCodeMappingRepository;
import com.alphapay.payEngine.integration.service.InitiatePaymentService;
import com.alphapay.payEngine.integration.service.PaymentGatewayService;
import com.alphapay.payEngine.integration.service.WorkflowService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import com.alphapay.payEngine.utilities.BeanUtility;
import com.alphapay.payEngine.utilities.InvoiceStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class TransactionStatusUpdateService {

    private final JacksonConfig jacksonConfig = new JacksonConfig();
    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;
    @Autowired
    private RestTemplate restTemplate;
    private MyFatoorahConfig config;
    @Autowired
    private PaymentLinkEntityRepository paymentLinkEntityRepository;
    @Autowired
    private BackEndResponseCodeMappingRepository mappingRepository;
    @Autowired
    private WorkflowService workflowOrchestratorService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InitiatePaymentService initiatePaymentService;

//    @Scheduled(fixedRate = 14400000)
    public void updateInvoiceStatuses() {
        List<PaymentLinkEntity> paymentLinks = paymentLinkEntityRepository.findByInvoiceStatus(InvoiceStatus.ACTIVE.getStatus());
        for (PaymentLinkEntity paymentLink : paymentLinks) {
            if (BeanUtility.isExpired(paymentLink.getExpiryDateTime())) {
                paymentLink.setInvoiceStatus(InvoiceStatus.EXPIRED.getStatus());
            }
        }
        paymentLinkEntityRepository.saveAll(paymentLinks);
        log.info("Scheduled task completed: Invoice statuses updated");
    }

    @Scheduled(fixedRateString = "${status.job.fixedRate.ms}")
    public void updateTransactionStatuses() {
        log.info("Scheduled task started: Updating transaction statuses");

        // Fetch last 5 transactions
        List<FinancialTransaction> transactions = financialTransactionRepository.findExecutePaymentTransactionsOlderThan10MinutesAndNewerThan3Days();
        if (transactions.isEmpty()) {
            log.info("No transactions found to update.");
            return;
        }

        for (FinancialTransaction transaction : transactions) {
            try {

                if (transaction.getExternalPaymentId() == null && transaction.getExternalInvoiceId() == null)
                    return;
                TransactionStatusResponse response = fetchPaymentStatus(transaction.getPaymentId());
                log.debug("FinancialTransactionResponse{}",response);

            } catch (Exception e) {
                log.error("Error updating transaction status for PaymentId: {}", transaction.getExternalPaymentId(), e);
            }
        }

        log.info("Scheduled task completed: Transaction statuses updated");
    }

    private TransactionStatusResponse fetchPaymentStatus(String paymentId) {
        PaymentStatusRequest request = new PaymentStatusRequest();
        request.setPaymentId(paymentId);
        request.setKeyType("PaymentId");
        request.setServiceId("130");
        request.setRequestId(UUID.randomUUID().toString());
         TransactionStatusResponse response=initiatePaymentService.processStatus( request, Boolean.TRUE) ;
        return response;

    }

    private ObjectNode mergeData(FinancialTransaction transaction) throws JsonProcessingException {
        FinancialTransaction financialTransaction1 = new FinancialTransaction();
        financialTransaction1.setExternalPaymentId(transaction.getExternalPaymentId());
        String createSupplierJson = jacksonConfig.objectMapper().writeValueAsString(financialTransaction1);
        ObjectNode mergedData = jacksonConfig.objectMapper().createObjectNode();
        mergedData.set("financialTransaction", jacksonConfig.objectMapper().readTree(createSupplierJson));

        return mergedData;
    }


    private void updateTransactionWithResponse(FinancialTransaction transaction, PaymentStatusResponse response) {
        PaymentStatusResponse.PaymentData data = response.getData();

        if (data != null && !data.getInvoiceTransactions().isEmpty()) {
            PaymentStatusResponse.PaymentData.InvoiceTransaction transactionData = data.getInvoiceTransactions().get(0);

            transaction.setTransactionStatus(transactionData.getTransactionStatus());
            transaction.setTransactionId(transactionData.getTransactionId());
            transaction.setExternalPaymentId(transactionData.getPaymentId());
            transaction.setTransactionTime(transactionData.getTransactionDate());

            transaction.setPaidCurrency(transactionData.getPaidCurrency());
            transaction.setPaidCurrencyValue(transactionData.getPaidCurrencyValue());
            transaction.setCountry(transactionData.getCountry());
            transaction.setDepositShare(response.getData().getSuppliers().get(0).getDepositShare());
            transaction.setTotalCharges(transactionData.getTransationValue().subtract(response.getData().getSuppliers().get(0).getDepositShare()));
            transaction.setVat(transactionData.getVatAmount());
            transaction.setCustomerServiceCharge(transactionData.getCustomerServiceCharge());
            transaction.setPaymentMethod(transactionData.getPaymentGateway());
            transaction.setInvoiceStatus(response.getData().getInvoiceStatus());

            if (transactionData.getTransactionStatus().equalsIgnoreCase("InProgress")) {
                transaction.setResponseMessage("The payment has not been completed and the customer is still in the process of entering the verification number or has cancelled the payment process.");
            }

            if (transactionData.getTransactionStatus().equalsIgnoreCase("succss") || transactionData.getTransactionStatus().equalsIgnoreCase("success")) {
                transaction.setTransactionStatus("Success");
                transaction.setResponseMessage("Payment Completed Successfully");
            }


            if (!transactionData.getErrorCode().isEmpty()) {
                String externalResponseCode = transactionData.getErrorCode();
                List<BackEndResponseCodeMapping> responseCodeMapping = mappingRepository.findByExternalResponseCode(externalResponseCode);
                if (!responseCodeMapping.isEmpty()) {
                    transaction.setResponseMessage(responseCodeMapping.get(0).getAppResponseMessage());
                }
            }

            if (!response.getMessage().isEmpty())
                transaction.setResponseMessage(response.getMessage());

            transaction.setInvoiceStatus(data.getInvoiceStatus());

            if (transaction.getVersion() > 5 && transactionData.getTransactionStatus().equals("InProgress")) {
                transaction.setTransactionStatus("Cancelled");
                transaction.setResponseMessage("Payment Cancelled.");
            }

            financialTransactionRepository.save(transaction);
            Optional<PaymentLinkEntity> paymentLinkEntity = paymentLinkEntityRepository.findByInvoiceId(transaction.getInvoiceLink());
            if (paymentLinkEntity.isPresent()) {

                if("Success".equalsIgnoreCase(transaction.getTransactionStatus()) && paymentLinkEntity.isPresent())
                {
                    if(paymentLinkEntity.get().getAdditionalInputs()!=null && !paymentLinkEntity.get().getAdditionalInputs().isEmpty())
                    {
                        initiatePaymentService.pushCredit(paymentLinkEntity.get(),transaction);
                    }
                }

                PaymentLinkEntity paymentLink = paymentLinkEntity.get();
                paymentLink.setExternalPaymentId(transactionData.getPaymentId());
                paymentLink.setInvoiceStatus(transaction.getInvoiceStatus());
                paymentLinkEntityRepository.save(paymentLink);
            }
            log.info("Updated transaction: {}", transaction.getId());
        }
    }
}