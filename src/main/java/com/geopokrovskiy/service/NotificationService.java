package com.geopokrovskiy.service;

import com.geopokrovskiy.entity.Notification;
import com.geopokrovskiy.entity.NotificationStatus;
import com.geopokrovskiy.repository.NotificationRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Data
@Service
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public Notification addNewNotification(Notification notification) {
        try {
            log.info("Saving notification {} to database", notification);
            return notificationRepository.save(notification);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.info("Notification has not been saved");
            throw new RuntimeException(e);
        }
    }

    public Notification finalizeNotification(Notification notification) {
        notification.setNotificationStatus(NotificationStatus.COMPLETED);
        return notificationRepository.save(notification);
    }

    public Page<Notification> getNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }
}
