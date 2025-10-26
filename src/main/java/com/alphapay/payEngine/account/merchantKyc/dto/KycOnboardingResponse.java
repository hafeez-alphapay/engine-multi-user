package com.alphapay.payEngine.account.merchantKyc.dto;

import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Data;

@Data
public class KycOnboardingResponse extends BaseResponse {
    Long merchantId;
}
