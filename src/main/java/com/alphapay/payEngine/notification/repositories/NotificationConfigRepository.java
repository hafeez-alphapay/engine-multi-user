package com.alphapay.payEngine.notification.repositories;

import com.alphapay.payEngine.notification.models.NotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationConfigRepository extends JpaRepository<NotificationConfig,Long> {
    boolean existsByApplicationId(String applicationId);
    NotificationConfig findByApplicationId(String applicationId);

}
