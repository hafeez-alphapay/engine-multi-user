package com.alphapay.payEngine.alphaServices.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "alphapay_services", uniqueConstraints = {
        @UniqueConstraint(columnNames = "service_id", name = "service_id")
})
@ToString
public class AlphaPayServicesEntity extends CommonBean {

    @Column(name = "service_name_ar", nullable = false, length = 50)
    private String serviceNameAr;

    @Column(name = "service_name_en", nullable = false, length = 50)
    private String serviceNameEn;

    @Column(name = "service_id", nullable = false, length = 255, unique = true)
    private String serviceId;
}
