package com.alphapay.payEngine.integration.dto.response;

import java.math.BigDecimal;

public class ChargesResult {
    public BigDecimal totalCharges;
    public BigDecimal providerCommissionWithVAT;
    public BigDecimal alphaPayCommission;

    public ChargesResult(BigDecimal totalCharges, BigDecimal providerCommissionWithVAT, BigDecimal alphaPayCommission) {
        this.totalCharges = totalCharges;
        this.providerCommissionWithVAT = providerCommissionWithVAT;
        this.alphaPayCommission = alphaPayCommission;
    }
}