package com.alphapay.payEngine.integration.model.orchast;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "service_workflow_step")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceWorkflowStep extends CommonBean {

    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private ServiceWorkflow serviceWorkflow;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    // Replace single steps with collections
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "workflow_success_transitions",
            joinColumns = @JoinColumn(name = "step_id"),
            inverseJoinColumns = @JoinColumn(name = "transition_id"))
    private Set<ServiceWorkflowTransition> successTransitions = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "workflow_failure_transitions",
            joinColumns = @JoinColumn(name = "step_id"),
            inverseJoinColumns = @JoinColumn(name = "transition_id"))
    private Set<ServiceWorkflowTransition> failureTransitions = new HashSet<>();

    @Column(name = "return_response", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Boolean returnResponse;
}
