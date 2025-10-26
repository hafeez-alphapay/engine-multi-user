package com.alphapay.payEngine.integration.dto.response;

import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ExchangeRateResponse extends BaseResponse {
    private ResponseData responseData;

    @Getter
    @Setter
    public static class ResponseData {
        private List<ExchangeRate> exchangeRates;
    }

    @Getter
    @Setter
    public static class ExchangeRate {
        private String rate;
        private String currency;
    }
}
