package com.alphapay.payEngine.service.bean;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.common.bean.AuditInfo;
import com.alphapay.payEngine.management.data.Application;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$",message = "Invalid UUID Format")
    private String requestId;

    private AuditInfo auditInfo;

    @JsonIgnore
    String applicationId;
    @JsonIgnore
    Application application;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+2")
    Date transactionDateTime;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserEntity user;
    //TODO: add this cause OptimusRequestLogger is invoked during response dispatch.should fix this in other way
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean filtered;


}
