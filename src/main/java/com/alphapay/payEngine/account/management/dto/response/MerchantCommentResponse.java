package com.alphapay.payEngine.account.management.dto.response;

import com.alphapay.payEngine.account.management.model.MerchantComment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MerchantCommentResponse {
    private List<MerchantComment> comments;
}
