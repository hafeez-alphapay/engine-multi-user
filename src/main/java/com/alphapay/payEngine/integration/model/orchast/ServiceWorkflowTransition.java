package com.alphapay.payEngine.integration.model.orchast;


import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workflow_transition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceWorkflowTransition extends CommonBean {


    @ManyToOne
    private ServiceEntity targetService;

    @Column(columnDefinition = "JSON")
    private String conditionExpression; // Velocity template for evaluating transition condition

    @Column(name = "is_default")
    private Boolean isDefault = false;
}