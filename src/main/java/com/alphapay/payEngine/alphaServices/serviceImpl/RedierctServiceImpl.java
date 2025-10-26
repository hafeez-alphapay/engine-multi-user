package com.alphapay.payEngine.alphaServices.serviceImpl;

import com.alphapay.payEngine.alphaServices.dto.request.Redirect3DSRequest;
import com.alphapay.payEngine.alphaServices.model.Redirect3DSUrl;
import com.alphapay.payEngine.alphaServices.repository.Redirect3DSUrlRepository;
import com.alphapay.payEngine.alphaServices.service.RedierctService;
import com.alphapay.payEngine.transactionLogging.data.FinancialTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class RedierctServiceImpl implements RedierctService {

    @Autowired
    private Redirect3DSUrlRepository redirect3DSUrlRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Override
    @Transactional
    public Redirect3DSUrl redirect(Redirect3DSRequest request) throws IOException {
        Redirect3DSUrl redirectObj = redirect3DSUrlRepository.findById(request.getUuid())
                .orElseThrow(() -> new IOException("Redirect URL not found for UUID: "
                        + request.getUuid() + ". Please check the UUID and try again."));
        String ipAddress= request.getIp();
        //TODO any business check to allow or reject the IP
        financialTransactionRepository.findByExternalPaymentIdAndTransactionType(
                        redirectObj.getExternalPaymentId(), "ExecutePaymentRequest")
                .ifPresent(tx -> tx.setIp(ipAddress));

        return redirectObj;
    }
}
