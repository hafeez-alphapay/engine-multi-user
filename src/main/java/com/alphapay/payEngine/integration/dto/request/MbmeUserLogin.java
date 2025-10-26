package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Setter
@Getter
@ToString
public class MbmeUserLogin extends BaseRequest {
    private Long merchantId;
    private String userName;
    private String password = "";
    private Integer hid;
    private Map<String,String> requestData;
}
