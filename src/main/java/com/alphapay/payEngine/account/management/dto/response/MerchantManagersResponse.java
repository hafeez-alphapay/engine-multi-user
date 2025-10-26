package com.alphapay.payEngine.account.management.dto.response;

import com.alphapay.payEngine.account.management.model.CountriesEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
public class MerchantManagersResponse {

    private String fullName;

    private String idNumber;

    private Date idExpiry;

    private Date dob;

    private String nationality;

    private String address;

    private String personType;

    private Boolean isShareholder;

    private String ownershipType;

    private Double ownershipPercentage;

    private String position;

    private String incomeSource;

    private String phone;

    private String email;

}
