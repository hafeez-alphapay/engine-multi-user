package com.alphapay.payEngine.alphaServices.dto.response;

import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CustomerCardBINInfoResponse extends BaseResponse {

    private String bin;

    private String brand;

    private String type;

    private String category;

    private String issuer;

    private String issuerPhone;

    private String issuerUrl;

    private String isoCode2;

    private String isoCode3;

    private String countryName;

}
