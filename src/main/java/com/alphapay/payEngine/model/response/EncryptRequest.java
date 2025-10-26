package com.alphapay.payEngine.model.response;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EncryptRequest extends BaseRequest {
    private String plainText;
}
