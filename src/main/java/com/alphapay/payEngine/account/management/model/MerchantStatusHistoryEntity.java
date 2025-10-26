package com.alphapay.payEngine.account.management.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "merchant_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantStatusHistoryEntity extends CommonBean{

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "entity_type", nullable = false)
    private String entityType; // e.g. USER_ACCOUNT, MANAGER_APPROVAL

    @Column(name = "new_status", nullable = false)
    private String newStatus;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Column(name = "assigned_user")
    private String assignedUser;

    @Column(name = "performed_by_user_id")
    private Long performedByUserId;

    @Column(name = "performed_by_user")
    private String performedByUser;

}
