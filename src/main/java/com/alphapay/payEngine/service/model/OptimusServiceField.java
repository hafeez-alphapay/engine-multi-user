package com.alphapay.payEngine.service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OptimusServiceField implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String fieldName;
    public Boolean mandatory;
    public String regex;
    public String fieldNameAr;
    public int length;
    public Long greaterThan;
    public Long lessThan;
    public String inCommaSeparatedList;
}
