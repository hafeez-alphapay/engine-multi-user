package com.alphapay.payEngine.alphaServices.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.*;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ValidateMerchantBINRequest extends BaseRequest {

    @NotNull(message = "merchantId is required")
    @Min(value = 1, message = "merchantId must be greater than 0")
    private Long merchantId;

    @NotBlank(message = "BIN is required")
    @Pattern(regexp = "\\d{6}", message = "BIN must be exactly 6 digits")
    private String bin;
}
