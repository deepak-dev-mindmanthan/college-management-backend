package org.collegemanagement.services;

import org.collegemanagement.entity.communication.Notification;

import java.util.List;

public interface NotificationService {
    Notification createNotification(Notification notification);
    List<Notification> getNotificationsByReceiverId(long receiverId);
    Notification findById(long id);
    Notification updateNotification(Notification notification);

}
