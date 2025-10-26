package com.alphapay.payEngine.integration.dto.paymentData;

import lombok.Data;

@Data
public class CardInfoRequest extends BaseFinancialRequest{
    private String paymentType = "card";
    private boolean bypass3DS = false;
    private Card card;
    private String paymentURL;


}
