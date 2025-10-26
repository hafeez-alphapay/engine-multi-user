package com.alphapay.payEngine.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Setter
@Getter
@MappedSuperclass
public abstract class CommonBean implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -3424777851855986787L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Integer version;
    @JsonIgnore
    private String status;

    @Column
    private LocalDateTime lastUpdated;
    @Column
    private LocalDateTime creationTime;

    public CommonBean() {
        super();
    }

    @PrePersist
    public void prePersist() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Dubai"));
        this.creationTime = now.toLocalDateTime();
        this.lastUpdated = now.toLocalDateTime();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = ZonedDateTime.now(ZoneId.of("Asia/Dubai")).toLocalDateTime();
    }
}
