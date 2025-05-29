package com.geopokrovskiy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "public", name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private String message;
    @Column(name = "user_uid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    private String subject;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    private String notificationDest;

    private String createdBy;
}
