package com.alphapay.payEngine.account.management.dto.request;

import jakarta.persistence.MappedSuperclass;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class GetCIFAccounts extends com.alphapay.payEngine.service.bean.BaseRequest {
    String cif;
    String inquiryPurpose;

}
