package com.alphapay.payEngine.management.data;

import com.alphapay.payEngine.account.management.model.FinancialInstitutions;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "applications")
@Getter
@Setter
public class Application implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_name", nullable = false)
    private String applicationName;
    @Column(name = "authorization_key", unique = true, nullable = false)
    private String authorizationKey;
    private String version;

    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", referencedColumnName = "application_id", insertable = false, updatable = false)
    private FinancialInstitutions financialInstitution;

}
