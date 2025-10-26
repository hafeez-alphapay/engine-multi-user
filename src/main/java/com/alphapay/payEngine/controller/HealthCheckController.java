package com.alphapay.payEngine.controller;


import com.alphapay.payEngine.model.response.StatusResponse;
import com.alphapay.payEngine.service.HealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HealthCheckController {

    @Autowired
    HealthCheckService healthCheckService;

    @GetMapping("/status")
    public StatusResponse status() {
        return healthCheckService.status();
    }
}
