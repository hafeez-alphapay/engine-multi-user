package com.alphapay.payEngine.model.response;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse extends BaseRequest {
    private String status;
    private int responseCode;
    private String responseMessage;
}
