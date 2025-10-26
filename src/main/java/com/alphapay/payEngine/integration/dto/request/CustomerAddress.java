package com.alphapay.payEngine.integration.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerAddress {
    private String addressBlock;
    private String addressStreet;
    private String addressHouseBuildingNo;
    private String address;
    private String addressInstructions;
}

