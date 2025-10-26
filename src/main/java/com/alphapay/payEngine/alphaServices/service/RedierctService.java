package com.alphapay.payEngine.alphaServices.service;

import com.alphapay.payEngine.alphaServices.dto.request.Redirect3DSRequest;
import com.alphapay.payEngine.alphaServices.model.Redirect3DSUrl;

import java.io.IOException;

public interface RedierctService {
    Redirect3DSUrl redirect( Redirect3DSRequest request) throws IOException;
}
