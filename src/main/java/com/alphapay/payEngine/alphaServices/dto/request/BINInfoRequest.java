package com.alphapay.payEngine.alphaServices.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BINInfoRequest extends BaseRequest {
    @NotBlank(message = "BIN is required")
    @Pattern(regexp = "\\d{6}", message = "BIN must be exactly 6 digits")
    private String bin;
}
