package com.alphapay.payEngine.integration.service;

import com.alphapay.payEngine.integration.model.MerchantPaymentProviderRegistration;

import java.util.List;

public interface ServiceProviderSwitcher {
    MerchantPaymentProviderRegistration determineBestProvider(List<MerchantPaymentProviderRegistration> providers,List<MerchantPaymentProviderRegistration> staticPreferredProviders);
}
