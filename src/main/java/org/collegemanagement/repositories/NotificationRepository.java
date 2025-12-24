package org.collegemanagement.repositories;

import org.collegemanagement.entity.communication.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findNotificationById(Long receiverId);
}
