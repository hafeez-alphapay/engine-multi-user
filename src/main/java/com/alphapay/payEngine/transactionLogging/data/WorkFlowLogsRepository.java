package com.alphapay.payEngine.transactionLogging.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkFlowLogsRepository extends JpaRepository<WorkFlowLogs, Long> {
}
