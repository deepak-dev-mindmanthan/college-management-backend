package org.collegemanagement.services;

import org.collegemanagement.entity.communication.Notification;
import org.collegemanagement.enums.NotificationReferenceType;
import org.collegemanagement.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    /**
     * Create and send a notification to a user
     */
    Notification createNotification(Long userId, String title, String content, NotificationType type, 
                                    NotificationReferenceType referenceType, Long referenceId, String actionUrl, int priority);

    /**
     * Create and send notifications to multiple users
     */
    List<Notification> createNotifications(List<Long> userIds, String title, String content, NotificationType type,
                                          NotificationReferenceType referenceType, Long referenceId, String actionUrl, int priority);

    /**
     * Mark notification as read
     */
    void markAsRead(Long notificationId);

    /**
     * Mark all notifications as read for a user
     */
    void markAllAsRead(Long userId);
}

