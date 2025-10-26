package com.alphapay.payEngine.common.encryption;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gateway_keys")
@Setter
@Getter
public class GatewayKeysEntity extends CommonBean {

    @Column
    private String publicKey;

    @Column
    private String privateKey;

    @Enumerated(EnumType.STRING)
    @Column
    private KeyType keyType;

}
