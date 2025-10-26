package com.alphapay.payEngine.service.model.repository;


import com.alphapay.payEngine.service.model.OptimusServiceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptimusServiceConfigRepository extends JpaRepository<OptimusServiceConfig, Long> {

    OptimusServiceConfig findById(long id);
    OptimusServiceConfig findByServiceId(String serviceId);

    OptimusServiceConfig findByServiceIdAndStatus(String serviceId,String status);
    OptimusServiceConfig findByApplicationIdAndServiceIdAndStatus(String applicationId,String serviceId,String status);


    List<OptimusServiceConfig> findByServiceName(String serviceName);

    List<OptimusServiceConfig> findByServiceNameAndStatus(String serviceName,String status);

    OptimusServiceConfig findByApplicationIdAndServiceNameAndStatus(String applicationId,String serviceName,String status);

}
