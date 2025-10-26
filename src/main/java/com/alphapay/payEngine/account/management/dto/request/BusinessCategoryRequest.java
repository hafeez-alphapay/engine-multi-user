package com.alphapay.payEngine.account.management.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BusinessCategoryRequest extends com.alphapay.payEngine.service.bean.BaseRequest{
    private Long businessTypeId;
}

