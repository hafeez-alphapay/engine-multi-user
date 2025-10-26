package com.alphapay.payEngine.account.roles.model;


import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "permissions", uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
public class PermissionEntity extends CommonBean {

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "description", length = 255)
    private String description;
}