package com.alphapay.payEngine.alphaServices.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "merchant_alphapay_services")
@ToString
public class MerchantAlphaPayServicesEntity extends CommonBean {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", referencedColumnName = "service_id", insertable = false, updatable = false)
    private AlphaPayServicesEntity alphaPayService;

    @Column(name = "service_id")
    private String serviceId;

    private Long merchantId;

}