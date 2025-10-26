package com.alphapay.payEngine.controller.alphaServices;

import com.alphapay.payEngine.alphaServices.dto.request.Redirect3DSRequest;
import com.alphapay.payEngine.alphaServices.model.Redirect3DSUrl;
import com.alphapay.payEngine.alphaServices.repository.Redirect3DSUrlRepository;
import com.alphapay.payEngine.alphaServices.service.RedierctService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
@Slf4j
@RestController
@RequestMapping("/redirect")
public class RedirectController {

    @Autowired
    RedierctService redierctService;

    @PostMapping(value = "/get3DSUrl", produces = "application/json", consumes = "application/json")
    public Redirect3DSUrl redirect(@RequestBody @Valid Redirect3DSRequest request) throws IOException {
        log.debug("Redirecting to 3DS URL for UUID: {} , IP:{}", request.getUuid(), request.getIp());
        return redierctService.redirect(request);
    }
}