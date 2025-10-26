package com.alphapay.payEngine.integration.service;

import com.alphapay.payEngine.account.management.dto.request.VendorPaymentMethod;
import com.alphapay.payEngine.account.management.dto.response.PaymentMethodResponse;
import com.alphapay.payEngine.integration.dto.request.CreateSupplier;
import com.alphapay.payEngine.integration.dto.request.CustomizeSupplierCommissions;
import com.alphapay.payEngine.integration.dto.response.BankResponse;
import com.alphapay.payEngine.integration.dto.response.ExchangeRateResponse;

import java.util.List;

public interface MerchantProviderRegistrationService {
    CreateSupplier createSupplier(CreateSupplier request) throws Exception;

    List<PaymentMethodResponse> getPaymentMethod();

    List<BankResponse> getAllBanks();

    CreateSupplier editSupplierDetails(CreateSupplier request);

    CreateSupplier customizeSupplierCommissions(CustomizeSupplierCommissions request);

    CreateSupplier uploadSupplierDocument(CreateSupplier request);
    void sendEmailToMFSupplier(CreateSupplier request);

    CreateSupplier getSupplierDeposits(CreateSupplier request);

    CreateSupplier GetSupplierDashboard(CreateSupplier request);

    ExchangeRateResponse getMfExchangeRate(CreateSupplier request);

    List<PaymentMethodResponse> getVendorPaymentMethod(VendorPaymentMethod request);
}
