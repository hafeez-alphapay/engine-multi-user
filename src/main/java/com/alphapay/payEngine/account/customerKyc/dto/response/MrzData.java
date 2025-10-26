package com.alphapay.payEngine.account.customerKyc.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MrzData {
    private int id;
    private int passport;
    private String mrz_type;
    private int valid_score;
    private String raw_text;
    private String type;
    private String country;
    private String number;
    private String date_of_birth;
    private String expiration_date;
    private String nationality;
    private String sex;
    private String names;
    private String surname;
    private String personal_number;
    private String check_number;
    private String check_date_of_birth;
    private String check_expiration_date;
    private String check_composite;
    private String check_personal_number;
    private boolean valid_number;
    private boolean valid_date_of_birth;
    private boolean valid_expiration_date;
    private boolean valid_composite;
    private boolean valid_personal_number;
    private String method;
    private LocalDateTime created_at;
}