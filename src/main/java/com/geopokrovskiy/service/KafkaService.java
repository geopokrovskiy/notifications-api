package com.geopokrovskiy.service;

import com.geopokrovskiy.entity.Notification;
import com.geopokrovskiy.entity.NotificationStatus;
import com.geopokrovskiy.entity.NotificationType;
import com.geopokrovskiy.mapper.mapstruct.notification.NotificationMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Data
@Component
public class KafkaService {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final NotificationMapper notificationMapper;

    @KafkaListener(topics = "#{'${kafka.topics}'.split(',')}")
    public void consume(GenericRecord record) {

        // Receiving a message from Kafka
        Notification notification = notificationMapper.fromGenericRecord(record);
        String subject = notification.getSubject();
        String message = notification.getMessage();
        NotificationType type = notification.getNotificationType();
        log.info("Received: subject={}, message={}, type={}", subject, message, type);


        // Saving the message to the Database
        notification.setCreatedAt(LocalDateTime.now());
        notification.setModifiedAt(LocalDateTime.now());
        notification.setNotificationStatus(NotificationStatus.NEW);

        Notification savedNotification = notificationService.addNewNotification(notification);
        log.info("Notification {} has been saved to database", savedNotification);


        // Sending an email
        try {
            int smtpResponse = emailService.sendEmail(subject, notification.getNotificationDest(), message);
            emailService.checkEmailStatus(smtpResponse);
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }

    }
}
