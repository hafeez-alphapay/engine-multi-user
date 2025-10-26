package com.alphapay.payEngine.integration.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "merchant_payment_methods")
@ToString
public class MerchantPaymentMethodsEntity extends CommonBean {
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodEntity paymentMethod;
}
