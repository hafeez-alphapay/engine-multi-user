package com.alphapay.payEngine.account.customerKyc.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Passport {
    private int id;
    private String image;
    private String number;
    private LocalDateTime created_at;
}
