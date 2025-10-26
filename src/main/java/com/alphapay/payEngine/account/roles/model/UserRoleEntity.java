package com.alphapay.payEngine.account.roles.model;

import com.alphapay.payEngine.account.management.model.UserEntity;
import com.alphapay.payEngine.common.bean.CommonBean;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Entity
@Table(name = "user_roles",
        indexes = {
                @Index(name = "user_id_index", columnList = "user_id"),
                @Index(name = "role_id_index", columnList = "role_id")
        })
@Setter
@Getter
@ToString
public class UserRoleEntity extends CommonBean {

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Many UserRoles belong to one User
    @JoinColumn(name = "user_id", nullable = false )
    @JsonBackReference
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Many UserRoles belong to one Role
    @JoinColumn(name = "role_id", nullable = false )
    private RoleEntity roleEntity;

    @Column(name = "assigned_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp assignedAt;
}
