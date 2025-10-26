package com.alphapay.payEngine.controller.customerkyc;

import com.alphapay.payEngine.account.customerKyc.dto.response.CustomerIdVerificationResponse;
import com.alphapay.payEngine.account.customerKyc.service.CustomerKycVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/kyc")
public class kycVerificationController {

    @Autowired
    private CustomerKycVerificationService customerKycVerificationService;

    @PostMapping(value = "/customerIdVerification", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CustomerIdVerificationResponse uploadCustomerId(@RequestParam("requestId") String requestId,
                                                           @RequestParam("acceptLanguage") String acceptLanguage,
                                                           @RequestParam("invoiceId") String invoiceId,
                                                           @RequestParam("idType") String idType,
                                                           @RequestParam("file") MultipartFile file,
                                                           HttpServletRequest httpServletRequest){
        return customerKycVerificationService.verifyCustomerIdDocument(idType,invoiceId, file, requestId, acceptLanguage);

    }
}
