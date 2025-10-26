package com.alphapay.payEngine.integration.dto.request.paymentGatewayIntegration;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GatewayConfigurationBaseRequest extends BaseRequest {
    private Long merchantId;
    private String serviceId;
}
