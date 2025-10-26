package com.alphapay.payEngine.management.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "password_policy")
@ToString
public class FinancialInstitutionPasswordPolicy implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigits;
    private boolean requireSpecialChars;
    private String policyName;

    private String policyRuleMessageEn;

    private String policyRuleMessageAr;

    private String applicationId;
}
