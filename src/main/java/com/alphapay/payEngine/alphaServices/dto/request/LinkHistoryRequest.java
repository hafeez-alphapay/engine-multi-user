package com.alphapay.payEngine.alphaServices.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LinkHistoryRequest extends BaseRequest {
    private Date fromDate;
    private Date toDate;
    private String status;
    private Long merchantId;
    private Integer pageNumber = 1;
    private Integer pageSize = 10;
    private String expiryDate;
    private String invoiceId;
    private String paymentLinkTitle;
    private String customerName;
    private BigDecimal amount;
    private String currency;
    private String invoiceStatus;
    private String linkType;
}
