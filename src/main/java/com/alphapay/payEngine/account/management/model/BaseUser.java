package com.alphapay.payEngine.account.management.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseUser extends CommonBean {

    /**
     *
     */
    private static final long serialVersionUID = -9011796470853569960L;

    @JsonUnwrapped
    @Embedded
    private UserDetails userDetails;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean locked;

    @Column
    private Date activationDate;

    @Column
    private Date disabledDate;

}
