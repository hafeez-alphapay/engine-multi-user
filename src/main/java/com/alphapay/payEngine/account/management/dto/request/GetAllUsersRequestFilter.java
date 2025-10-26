package com.alphapay.payEngine.account.management.dto.request;

import com.alphapay.payEngine.service.bean.BaseRequest;
import lombok.*;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GetAllUsersRequestFilter extends BaseRequest {
    private Date fromDate;
    private Date toDate;
    private Integer pageSize;
    private Integer pageNumber;
    private String roleId;
    private Long assignTo;
    private String tradeNameEnglish;
    private String address;
    private String fullName;
    private String mobileNo;
    private String email;
    private String adminApproveStatus;
    private String managerApproveStatus;
    private String mbmeApproveStatus;
    private String myfattoraApproveStatus;
    private Long userId;
    private Long merchantId;
    private Boolean isAdmin=Boolean.TRUE;
    private Long parentId;
    private List<Long> subUserIds;
}
