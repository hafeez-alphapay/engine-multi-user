package com.alphapay.payEngine.integration.dto.paymentData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CustomerAddressRequest {
    private String block;
    private String street;
    private String houseBuildingNo;
    private String address;
    private String addressInstructions;
}
