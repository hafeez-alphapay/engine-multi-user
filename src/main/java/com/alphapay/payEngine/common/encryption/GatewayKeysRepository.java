package com.alphapay.payEngine.common.encryption;

import com.alphapay.payEngine.common.encryption.GatewayKeysEntity;
import com.alphapay.payEngine.common.encryption.KeyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayKeysRepository extends JpaRepository<GatewayKeysEntity, Long> {

    GatewayKeysEntity findByKeyType(KeyType keyType);
}
