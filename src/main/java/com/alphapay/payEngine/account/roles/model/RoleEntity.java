package com.alphapay.payEngine.account.roles.model;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.common.bean.CommonBean;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "roles", uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Setter
@Getter
public class RoleEntity extends CommonBean {

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonBackReference
    private Set<UserEntity> users;

    @OneToMany(mappedBy = "roleEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RolePermissionEntity> rolePermissions;

    @Override
    public String toString() {
        return "RoleEntity{" +
                "name='" + name + '\'' +
                ", rolePermissions=" + rolePermissions +
                '}';
    }
}
