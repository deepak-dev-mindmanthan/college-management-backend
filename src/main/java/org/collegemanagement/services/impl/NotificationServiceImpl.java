package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.entity.communication.Notification;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.NotificationReferenceType;
import org.collegemanagement.enums.NotificationType;
import org.collegemanagement.repositories.NotificationRepository;
import org.collegemanagement.repositories.UserRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TenantAccessGuard tenantAccessGuard;

    @Override
    @Transactional
    public Notification createNotification(Long userId, String title, String content, NotificationType type,
                                          NotificationReferenceType referenceType, Long referenceId, String actionUrl, int priority) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Validate user belongs to current tenant
        if (user.getCollege() != null && !user.getCollege().getId().equals(collegeId)) {
            throw new IllegalArgumentException("User does not belong to current tenant");
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .actionUrl(actionUrl)
                .priority(priority)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public List<Notification> createNotifications(List<Long> userIds, String title, String content, NotificationType type,
                                                  NotificationReferenceType referenceType, Long referenceId, String actionUrl, int priority) {
        List<Notification> notifications = new ArrayList<>();
        for (Long userId : userIds) {
            try {
                Notification notification = createNotification(userId, title, content, type, referenceType, referenceId, actionUrl, priority);
                notifications.add(notification);
            } catch (Exception e) {
                log.warn("Failed to create notification for user {}: {}", userId, e.getMessage());
            }
        }
        return notifications;
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + notificationId));

        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate notification belongs to current tenant
        if (notification.getUser().getCollege() != null && !notification.getUser().getCollege().getId().equals(collegeId)) {
            throw new IllegalArgumentException("Notification does not belong to current tenant");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserIdAndCollegeId(userId, collegeId);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
}

