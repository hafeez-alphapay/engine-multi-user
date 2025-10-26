package com.alphapay.payEngine.financial.reserve;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RollingReserveRepository extends JpaRepository<RollingReserve, Long> {
}
