package com.alphapay.payEngine.alphaServices.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Data;

@Data
public class Redirect3DSRequest extends BaseRequest {
    String uuid;
    String ip;
}
