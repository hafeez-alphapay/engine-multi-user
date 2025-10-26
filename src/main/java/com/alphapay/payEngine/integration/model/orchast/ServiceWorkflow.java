package com.alphapay.payEngine.integration.model.orchast;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_workflow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceWorkflow extends CommonBean {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id", referencedColumnName = "id", nullable = false)
    private ServiceProvider serviceProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initial_service_id", referencedColumnName = "id", nullable = false)
    private ServiceEntity initialServiceEntity;
}
