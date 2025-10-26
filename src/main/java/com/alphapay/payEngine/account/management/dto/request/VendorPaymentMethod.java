package com.alphapay.payEngine.account.management.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VendorPaymentMethod extends BaseRequest {
    @NotBlank(message = "apiKey is required")
    private String apiKey;
    private Long merchantId;
}
