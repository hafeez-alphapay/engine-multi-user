package com.alphapay.payEngine.account.management.dto.response;

import com.alphapay.payEngine.storage.dto.DocumentDescription;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MerchantDetailsResponse extends MerchantResponse{
    private List<DocumentDescription> documentsCategory;
    private MerchantServiceConfigDTO merchantConfig;

}

