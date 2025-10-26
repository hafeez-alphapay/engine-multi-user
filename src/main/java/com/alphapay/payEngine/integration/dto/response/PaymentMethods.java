package com.alphapay.payEngine.integration.dto.response;

import com.alphapay.payEngine.integration.model.PaymentMethodEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class PaymentMethods {
    private Integer PaymentMethodId;
    private String PaymentMethodAr;
    private String PaymentMethodEn;
    private String PaymentMethodCode;
    private Boolean IsDirectPayment;
    private Double ServiceCharge;
    private Double TotalAmount;
    private String CurrencyIso;
    private Boolean IsEmbeddedSupported;
    private String PaymentCurrencyIso;

    public List<PaymentMethods> convertToDto(List<PaymentMethodEntity> paymentMethodEntities) {
        return paymentMethodEntities.stream().map(entity -> {
            PaymentMethods dto = new PaymentMethods();
            dto.setPaymentMethodId(entity.getPaymentMethodId());
            dto.setPaymentMethodAr(entity.getPaymentMethodAr());
            dto.setPaymentMethodEn(entity.getPaymentMethodEn());
            dto.setPaymentMethodCode(entity.getPaymentMethodCode());
            dto.setIsDirectPayment(entity.getIsDirectPayment());
            dto.setServiceCharge(entity.getServiceCharge() != null ? entity.getServiceCharge().doubleValue() : 0.0);
            dto.setTotalAmount(entity.getTotalAmount() != null ? entity.getTotalAmount().doubleValue() : 0.0);
            dto.setCurrencyIso(entity.getCurrencyIso());
            dto.setIsEmbeddedSupported(entity.getIsEmbeddedSupported());
            dto.setPaymentCurrencyIso(entity.getPaymentCurrencyIso());
            return dto;
        }).collect(Collectors.toList());
    }
}
