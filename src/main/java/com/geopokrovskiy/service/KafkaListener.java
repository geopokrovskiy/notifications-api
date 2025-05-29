package com.geopokrovskiy.service;

import com.geopokrovskiy.entity.Notification;
import com.geopokrovskiy.entity.NotificationStatus;
import com.geopokrovskiy.entity.NotificationType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Data
@Component
public class KafkaListener {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @org.springframework.kafka.annotation.KafkaListener(topics = "sign_up")
    public void consume(GenericRecord record) {

        // Receiving a message from Kafka
        String subject = record.get("subject").toString();
        String message = record.get("message").toString();
        String type = record.get("notification_type").toString();
        String destination = record.get("notification_dest").toString();
        String createdBy = record.get("created_by").toString();

        log.info("Received: subject={}, message={}, type={}", subject, message, type);


        // Saving the message to the Database
        Notification notification = new Notification().toBuilder()
                .subject(subject)
                .message(message)
                .notificationType(NotificationType.valueOf(type))
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .createdBy(createdBy)
                .notificationStatus(NotificationStatus.NEW)
                .notificationDest(destination)
                .build();

        Notification savedNotification = notificationService.addNewNotification(notification);
        log.info("Notification {} has been saved to database", savedNotification);


        // Sending an email
        try {
            int smtpResponse = emailService.sendEmail(subject, destination, message);
            if (smtpResponse == 250) {
                log.info("Email sent successfully");

                // Saving the message in the database with Status.COMPLETED
                notificationService.finalizeNotification(savedNotification);
                log.info("Notification treatment {} has been finalized", savedNotification);
            } else {
                log.error("Failed to send email. Status code: {}", smtpResponse);
            }
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }

    }
}
