package com.alphapay.payEngine.controller.supplier;

import com.alphapay.payEngine.account.management.dto.request.VendorPaymentMethod;
import com.alphapay.payEngine.account.management.dto.response.PaymentMethodResponse;
import com.alphapay.payEngine.integration.dto.request.CreateSupplier;
import com.alphapay.payEngine.integration.dto.request.CustomizeSupplierCommissions;
import com.alphapay.payEngine.integration.dto.request.MyFatoorahaWebhookRequest;
import com.alphapay.payEngine.integration.dto.response.BankResponse;
import com.alphapay.payEngine.integration.dto.response.ExchangeRateResponse;
import com.alphapay.payEngine.integration.service.MerchantProviderRegistrationService;
import com.alphapay.payEngine.integration.service.MyfatoorahWebHookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class MyFatoorahaController {
    @Autowired
    private MerchantProviderRegistrationService merchantProviderRegistrationService;

    @Autowired
    private MyfatoorahWebHookService myfatoorahWebHookService;

    @PostMapping("/suppliers/create")
    public CreateSupplier createSupplier(@RequestBody CreateSupplier request) throws Exception {
        return merchantProviderRegistrationService.createSupplier(request);
    }

    @PostMapping("/suppliers/uploadDocument")
    public CreateSupplier uploadSupplierDocument(@RequestBody CreateSupplier request) throws Exception {
        return merchantProviderRegistrationService.uploadSupplierDocument(request);
    }

    @PostMapping("/suppliers/editSupplier")
    public CreateSupplier editSupplier(@RequestBody CreateSupplier request) throws Exception {
        return merchantProviderRegistrationService.editSupplierDetails(request);
    }

    @PostMapping("/suppliers/customizeSupplierCommissions")
    public CreateSupplier customizeSupplierCommissions(@RequestBody CustomizeSupplierCommissions request) throws Exception {
        return merchantProviderRegistrationService.customizeSupplierCommissions(request);
    }

    @PostMapping("/suppliers/sendEmailToMFSupplier")
    public void sendEmailToMFSupplier(@RequestBody CreateSupplier request) throws Exception {
        merchantProviderRegistrationService.sendEmailToMFSupplier(request);
    }

    @PostMapping("/suppliers/getSupplierDeposits")
    public CreateSupplier getSupplierDeposits(@RequestBody CreateSupplier request) throws Exception {
        return merchantProviderRegistrationService.getSupplierDeposits(request);
    }

    @PostMapping("/suppliers/getSupplierDashboard")
    public CreateSupplier getSupplierDashboard(@RequestBody CreateSupplier request) throws Exception {
        return merchantProviderRegistrationService.GetSupplierDashboard(request);
    }

    @PostMapping("/suppliers/paymentMethod")
    public List<PaymentMethodResponse> getPaymentMethod() throws Exception {
        return merchantProviderRegistrationService.getPaymentMethod();
    }

    @PostMapping("/suppliers/vendorPaymentMethod")
    public List<PaymentMethodResponse> getVendorPaymentMethod(@RequestBody VendorPaymentMethod request) throws Exception {
        return merchantProviderRegistrationService.getVendorPaymentMethod(request);
    }

    @PostMapping("/suppliers/banks")
    public ResponseEntity<List<BankResponse>> getAllBanks() {
        List<BankResponse> response = merchantProviderRegistrationService.getAllBanks();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/myFatoorahaWebHook")
    public void myFatoorahaWebHook(@RequestBody MyFatoorahaWebhookRequest request) throws Exception {
        myfatoorahWebHookService.processWebHookResponse(request);

    }


    @PostMapping("/suppliers/exchangeRate")
    public ExchangeRateResponse getMfExchangeRate(@RequestBody CreateSupplier request) throws Exception {
        return merchantProviderRegistrationService.getMfExchangeRate(request);
    }

}