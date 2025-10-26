package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.alphaServices.serviceImpl.BINServiceImpl;
import com.alphapay.payEngine.integration.dto.response.ChargesResult;
import com.alphapay.payEngine.integration.model.CustomMerchantCommissionEntity;
import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;
import com.alphapay.payEngine.integration.model.PaymentMethodEntity;
import com.alphapay.payEngine.integration.model.orchast.ServiceProvider;
import com.alphapay.payEngine.integration.repository.MerchantProviderRegistrationRepository;
import com.alphapay.payEngine.integration.repository.PaymentMethodRepository;
import com.alphapay.payEngine.integration.repository.ServiceProviderRepository;
import com.alphapay.payEngine.integration.service.ChargesCalculatorService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransaction;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ChargesCalculatorServiceImpl implements ChargesCalculatorService {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.05");
    @Value("${providers.mbme.id}")
    Long mbmeId;
    @Value("${providers.myFatoorah.id}")
    Long myFatoorahId;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;
    @Autowired
    private MerchantProviderRegistrationRepository merchantProviderRegistrationRepository;
    @Autowired
    private BINServiceImpl binService;
    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    public ChargesResult calculateCharges(FinancialTransaction transaction, String paymentId) {
        if (transaction == null) {
            transaction = financialTransactionRepository.findByPaymentIdAndTransactionType(paymentId,"ExecutePaymentRequest").get();
        }
        BigDecimal paidCurrencyValue = transaction.getPaidCurrencyValue();
        String bin = extractBin(transaction.getCardNumber());
        String paymentMethod = transaction.getPaymentMethod();
        Long providerId = transaction.getProcessorId();
        Optional<ServiceProvider> serviceProvider;

        if (Objects.equals(providerId, mbmeId)) {
            serviceProvider = serviceProviderRepository.findById(providerId);
        } else if (Objects.equals(providerId, myFatoorahId)) {
            serviceProvider = serviceProviderRepository.findById(providerId);
        } else {
            serviceProvider = serviceProviderRepository.findById(myFatoorahId);
        }

        if (paymentMethod.toLowerCase().contains("card")) {
            paymentMethod = "Cards";
        }

        boolean isLocal = isLocalCard(bin);
        log.debug("paymentMethod---------------->: {}" , paymentMethod);
        log.debug("serviceProvider---------------->: {}" , serviceProvider.get());

        Optional<PaymentMethodEntity> paymentMethodEntity = paymentMethodRepository.findByPaymentMethodEnAndServiceProvider(paymentMethod, serviceProvider.get());

        PaymentMethodEntity transactionPaymentMethod = paymentMethodEntity.get();

        Optional<MerchantPaymentProviderRegistration> merchantPaymentProviderRegistration = merchantProviderRegistrationRepository.findByServiceProviderAndMerchantId(serviceProvider.get(), transaction.getMerchantId());

        BigDecimal fixedAlphaPayCommission = merchantPaymentProviderRegistration.get().getCommissionValue();
        BigDecimal percentageAlphaPayCommission = merchantPaymentProviderRegistration.get().getCommissionPercentage();

        List<CustomMerchantCommissionEntity> merchantCommissionEntities = merchantPaymentProviderRegistration.get().getCustomCommissions();

        for (CustomMerchantCommissionEntity customCommission : merchantCommissionEntities) {
            if (Objects.equals(customCommission.getPaymentMethod().getId(), transactionPaymentMethod.getId())) {
                if (customCommission.isLocal() == isLocal) {
                    fixedAlphaPayCommission = customCommission.getCommissionValue();
                    percentageAlphaPayCommission = customCommission.getCommissionPercentage();
                } else {
                    fixedAlphaPayCommission = customCommission.getCommissionValue();
                    percentageAlphaPayCommission = customCommission.getCommissionPercentage();
                }
            }

        }


        BigDecimal providerCommissionRate = isLocal ? transactionPaymentMethod.getLocalPercentageComm() : transactionPaymentMethod.getInternationalPercentageComm();
        BigDecimal providerCommission = (paidCurrencyValue.multiply(providerCommissionRate)).divide(new BigDecimal("100"), RoundingMode.HALF_UP);

        BigDecimal providerVat = providerCommission.multiply(VAT_RATE);
        BigDecimal providerCommissionWithVAT = providerCommission.add(providerVat);

        BigDecimal alphaPayCommissionPercentage = (paidCurrencyValue.multiply(percentageAlphaPayCommission)).divide(new BigDecimal("100"), RoundingMode.HALF_UP);
        BigDecimal totalAlphaPayCommission = alphaPayCommissionPercentage.add(fixedAlphaPayCommission);
        BigDecimal totalCharges = providerCommissionWithVAT.add(totalAlphaPayCommission);

        return new ChargesResult(totalCharges, providerCommissionWithVAT, totalAlphaPayCommission);
    }

    private String extractBin(String cardNumber) {
        return cardNumber != null && cardNumber.length() >= 6 ? cardNumber.substring(0, 6) : "";
    }

    private boolean isLocalCard(String bin) {
        String isoCode = binService.getCountryISO2FromBin(bin);
        if (isoCode != null && !isoCode.isBlank()) {
            return "AE".equalsIgnoreCase(isoCode);
        } else {
            return false;
        }
    }
}
