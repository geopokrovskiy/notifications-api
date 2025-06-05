package com.geopokrovskiy.rest;

import com.geopokrovskiy.dto.notification_service.notification.NotificationResponseDto;
import com.geopokrovskiy.entity.Notification;
import com.geopokrovskiy.mapper.mapstruct.notification.NotificationMapper;
import com.geopokrovskiy.service.NotificationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Data
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @GetMapping("/list")
    public ResponseEntity<Page<NotificationResponseDto>> getAllNotifications(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                             @RequestParam(name = "size", defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<NotificationResponseDto> notifications = notificationService.getNotifications(pageRequest).map(notificationMapper::map);
        return new ResponseEntity<>(notifications, HttpStatusCode.valueOf(200));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDto> getNotification(@PathVariable String id) {
        try {
            Long notificationId = Long.parseLong(id);
            Notification notification = notificationService.getNotificationById(notificationId);
            if (notification == null) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.ok(notificationMapper.map(notification));
            }
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

}
