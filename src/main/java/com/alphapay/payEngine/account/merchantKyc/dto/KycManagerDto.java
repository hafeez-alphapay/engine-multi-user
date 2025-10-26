package com.alphapay.payEngine.account.merchantKyc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Date;

/**
 * Represents a manager/UBO for KYB onboarding request.
 */
@Data
public class KycManagerDto {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "ID number is required")
    private String idNumber;

    @NotNull(message = "ID expiry date is required")
    private Date idExpiry;

    private Date dob;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Person type is required")
    private String personType;

    @NotNull(message = "Shareholder status must be specified")
    private Boolean isShareholder;

    // Optional unless isShareholder = true (enforced in service logic)
    private String ownershipType;

    private Double ownershipPercentage;

    private String position;

    private String incomeSource;

    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}