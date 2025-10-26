package com.alphapay.payEngine.notification.repositories;

import  com.alphapay.payEngine.notification.models.Notification;
import org.springframework.data.repository.CrudRepository;

public interface NotificationRepository  extends CrudRepository<Notification,Long> {
}
