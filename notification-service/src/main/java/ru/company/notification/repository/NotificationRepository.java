package ru.company.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.company.notification.entity.Notification;
import ru.company.notification.entity.NotificationStatus;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    Page<Notification> findByUserId(String userId, Pageable pageable);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);

    long countByStatus(NotificationStatus status);
}