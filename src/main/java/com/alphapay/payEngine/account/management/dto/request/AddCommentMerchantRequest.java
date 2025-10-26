package com.alphapay.payEngine.account.management.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddCommentMerchantRequest extends BaseRequest {
    private Long merchantId;
    private String commentBy;
    private String typeOfComment;
    private String comment;
}
