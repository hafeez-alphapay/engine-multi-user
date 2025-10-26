package com.alphapay.payEngine.common.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError {

    private String propertyName;
    private String propertyValue;
    private String message;

}
