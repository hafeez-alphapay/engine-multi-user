package com.alphapay.payEngine.common.bean;

import com.alphapay.payEngine.common.validator.ValidDeviceId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseAuditableAPIModel {
    @NotBlank String requestId;
    @ValidDeviceId
    AuditInfo auditInfo;
 }
