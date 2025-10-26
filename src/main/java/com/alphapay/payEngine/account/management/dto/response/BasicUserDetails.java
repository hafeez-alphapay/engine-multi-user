package com.alphapay.payEngine.account.management.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Setter
@Getter
public class BasicUserDetails {
    private Long userId;
    private String fullName;
    private String mobileNo;
    private String email;
    private String roleName;
    private Long roleId;
    private boolean enabled;
    private boolean locked;
    private int loginTryCount;
    private Date activationDate;
    private Date disabledDate;
    private Date lastLogin;
}
