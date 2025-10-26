package com.alphapay.payEngine.common.bean;

import lombok.Data;

@Data
public class VerifyResult {
    private final boolean valid;
    private final String  message;
}
