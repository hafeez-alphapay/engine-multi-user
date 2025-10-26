package com.alphapay.payEngine.account.management.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class TypeResponse {
    private List<BusinessTypeResponse> businessTypes;
}
