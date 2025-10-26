package com.alphapay.payEngine.service.model;

import com.alphapay.payEngine.service.model.repository.ExternalStepType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Entity
public class ExternalExecutionStep implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int stepId;
    private String backEndServiceName ;

    private String reversalServiceName ;

    private String statusServiceName ;

    private ExternalStepType type;
}
