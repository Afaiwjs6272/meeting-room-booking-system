package ru.company.notification.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.company.common.event.NotificationEvent;
import ru.company.notification.service.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "notification-events", groupId = "notification-group")
    public void consumeNotificationEvent(NotificationEvent event) {
        log.info("Received notification event: type={}, userId={}",
                event.getEventType(), event.getUserId());

        try {
            notificationService.processNotification(event);
        } catch (Exception e) {
            log.error("Failed to process notification: {}", e.getMessage(), e);
        }
    }
}