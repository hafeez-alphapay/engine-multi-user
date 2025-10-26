package com.alphapay.payEngine.account.management.model;

import com.alphapay.payEngine.account.roles.model.RoleEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Setter
@Getter
public class UserEntity extends BaseUser {

    private Date lastLogin;

    @Column(nullable = false, unique = true)
    private Long registrationId;

    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "push_notification_id")
    private String pushNotificationId;

    private int loginTryCount;
    private String requestId;

    @JsonBackReference
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles;


    /**
     * Many sub‑merchants can reference one parent merchant.
     */
    @JsonBackReference               // prevents infinite JSON loop on parent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id")   // FK to users.id
    private UserEntity parentUser;        // null ⇒ this user *is* the master

    /**
     * Master → list of subs (inverse side)
     */
    @JsonManagedReference             // paired with @JsonBackReference
    @OneToMany(mappedBy = "parentUser", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserEntity> subUsers = new HashSet<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMfaConfig> mfaConfigs;

    private String passwordHistory;

    /*
    ALTER TABLE users
    ADD COLUMN parent_id BIGINT      NULL,
    ADD CONSTRAINT fk_users_parent
        FOREIGN KEY (parent_id)
        REFERENCES users(id)
        ON DELETE SET NULL;

     */

    @Override
    public String toString() {
        return "UserEntity{" +
                "lastLogin=" + lastLogin +
                ", registrationId=" + registrationId +
                ", applicationId='" + applicationId + '\'' +
                ", pushNotificationId='" + pushNotificationId + '\'' +
                ", loginTryCount=" + loginTryCount +
                ", requestId='" + requestId + '\'' +
                ", roles=" + roles +
                '}';
    }
}
