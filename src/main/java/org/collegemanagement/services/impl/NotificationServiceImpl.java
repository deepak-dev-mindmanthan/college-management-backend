package org.collegemanagement.services.impl;

import org.collegemanagement.entity.Notification;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.NotificationRepository;
import org.collegemanagement.services.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsByReceiverId(long receiverId) {
        return notificationRepository.findByReceiverId(receiverId);
    }

    @Override
    public Notification findById(long id) {
        return notificationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No notification found with id: " + id));
    }

    @Transactional
    @Override
    public Notification updateNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

}
