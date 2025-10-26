package com.alphapay.payEngine.alphaServices.serviceImpl;


import com.alphapay.payEngine.alphaServices.dto.response.CustomerCardBINInfoResponse;
import com.alphapay.payEngine.alphaServices.model.BinData;
import com.alphapay.payEngine.alphaServices.repository.BinDataRepository;
import com.alphapay.payEngine.utilities.BeanUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BINServiceImpl {

    @Autowired
    private BinDataRepository binDataRepository;

    @Transactional(readOnly = true)
    public String getCountryISO2FromBin(String bin) {
        return binDataRepository.findIsoCode2ByBin(bin);
    }

    public CustomerCardBINInfoResponse getBinInfo(String bin) {
        BinData binData = binDataRepository.findByBin(bin);
        CustomerCardBINInfoResponse customerCardBINInfoResponse = new CustomerCardBINInfoResponse();
        BeanUtility.copyProperties(binData,customerCardBINInfoResponse);
        return customerCardBINInfoResponse;
    }
}
