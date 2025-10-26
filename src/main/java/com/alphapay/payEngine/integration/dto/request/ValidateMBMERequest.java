package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateMBMERequest extends BaseRequest {

    private Map<String,Object> mbmeResponse;


}
