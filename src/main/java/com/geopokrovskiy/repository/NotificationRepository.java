package com.geopokrovskiy.repository;

import com.geopokrovskiy.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
