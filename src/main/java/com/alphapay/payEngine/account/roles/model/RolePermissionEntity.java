package com.alphapay.payEngine.account.roles.model;


import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "role_permissions",
        indexes = {
                @Index(name = "role_id_index", columnList = "role_id"),
                @Index(name = "permission_id_index", columnList = "permission_id")
        })
@Setter
@Getter
public class RolePermissionEntity extends CommonBean {

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Many RolePermissions belong to one Role
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "role_permissions_ibfk_1"))
    private RoleEntity roleEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Many RolePermissions belong to one Permission
    @JoinColumn(name = "permission_id", nullable = false, foreignKey = @ForeignKey(name = "role_permissions_ibfk_2"))
    private PermissionEntity permissionEntity;

}
