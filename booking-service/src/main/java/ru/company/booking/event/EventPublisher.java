package ru.company.booking.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.company.common.event.BookingEvent;
import ru.company.common.event.NotificationEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.booking-events:booking-events}")
    private String bookingEventsTopic;

    @Value("${app.kafka.topics.notification-events:notification-events}")
    private String notificationEventsTopic;

    public void publishBookingEvent(BookingEvent event) {
        log.info("Publishing booking event: {} for booking: {}", event.getEventType(), event.getBookingId());
        kafkaTemplate.send(bookingEventsTopic, event.getBookingId(), event);
    }

    public void publishNotificationEvent(NotificationEvent event) {
        log.info("Publishing notification event: {} for user: {}", event.getEventType(), event.getUserId());
        kafkaTemplate.send(notificationEventsTopic, event.getUserId(), event);
    }
}