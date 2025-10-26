package com.alphapay.payEngine.service.impl;

import com.alphapay.payEngine.model.response.StatusResponse;
import com.alphapay.payEngine.service.HealthCheckService;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckServiceImpl implements HealthCheckService {


    @Override
    public StatusResponse status() {

        return new StatusResponse("up");
    }
}
