package com.alphapay.payEngine.controller.account;

import com.alphapay.payEngine.common.encryption.EncryptionService;
import com.alphapay.payEngine.model.response.EncryptRequest;
import com.alphapay.payEngine.model.response.EncryptResponse;
import com.alphapay.payEngine.model.response.EncryptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/encryption")
@Slf4j
public class EncryptionController {

    @Autowired
    EncryptionService encryptionService;

    @PostMapping("/getpublickey")
    EncryptionResponse getPublicKey() {
        return encryptionService.getPublicKey();
    }

    @PostMapping("/encrypt")
    EncryptResponse encryptText(@RequestBody EncryptRequest request) {
        return encryptionService.encrypt(request.getPlainText());
    }
}
