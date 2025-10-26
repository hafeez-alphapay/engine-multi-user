package com.alphapay.payEngine.common.otp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TOTPKey {
    private String key;
    private String qrCodeImage;
    private String provider;
    private String providerUrl="https://merchant.alphapay.ae"; //provide_host_name

    String cif;

    public TOTPKey(String key, String qrCodeImage,String provider,String cif) {
        this.key = key;
        this.qrCodeImage = qrCodeImage;
        this.provider = provider;
        this.cif = cif;
    }


}
