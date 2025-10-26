package com.alphapay.payEngine.service.model.repository;

import com.alphapay.payEngine.service.model.OptimusServiceField;
import org.springframework.data.repository.CrudRepository;

public interface OptimusServiceFieldRepository extends CrudRepository<OptimusServiceField, Long> {

    OptimusServiceField findById(long id);
}
