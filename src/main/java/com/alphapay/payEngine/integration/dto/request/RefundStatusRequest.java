package com.alphapay.payEngine.integration.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RefundStatusRequest extends BaseRequest {
    @NotBlank(message = "apiKey is required")
    private String apiKey;
    @NotBlank(message = "paymentId is required")
    private String paymentId;

}
