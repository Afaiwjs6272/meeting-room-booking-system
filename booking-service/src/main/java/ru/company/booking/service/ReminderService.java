package ru.company.booking.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.company.booking.client.ReferenceServiceClient;
import ru.company.booking.entity.Booking;
import ru.company.booking.event.EventPublisher;
import ru.company.booking.repository.BookingRepository;
import ru.company.common.event.NotificationEvent;
import ru.company.common.reference.EmployeeResponse;
import ru.company.common.reference.RoomResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final BookingRepository bookingRepository;
    private final ReferenceServiceClient referenceServiceClient;
    private final EventPublisher eventPublisher;

    @Value("${reminder.minutes-before:15}")
    private int reminderMinutesBefore;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void sendReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderTimeStart = now.plusMinutes(reminderMinutesBefore);
            LocalDateTime reminderTimeEnd = reminderTimeStart.plusMinutes(1);

            log.info("Current time: {}", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info("Looking for bookings with start_time between:");
            log.info("  - Start range: {}", reminderTimeStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info("  - End range:   {}", reminderTimeEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            List<Booking> allFutureBookings = bookingRepository.findByStartTimeAfter(now);
            log.info("Total future bookings (start_time > now): {}", allFutureBookings.size());

            for (Booking b : allFutureBookings) {
                long minutesUntilStart = java.time.Duration.between(now, b.getStartTime()).toMinutes();
                log.info("  - Booking {} starts at {} (in {} minutes), reminder_sent={}",
                        b.getId().substring(0, 8),
                        b.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        minutesUntilStart,
                        b.getReminderSent());
            }

            List<Booking> bookings = bookingRepository.findBookingsNeedingReminder(reminderTimeStart, reminderTimeEnd);

            log.info("========================================");
            log.info("Found {} bookings needing reminder", bookings.size());
            log.info("========================================");

            if (bookings.isEmpty()) {
                log.info("No bookings need reminder at this time");
                return;
            }

            for (Booking booking : bookings) {
                log.info("Processing booking: id={}, title={}, start_time={}",
                        booking.getId(),
                        booking.getTitle(),
                        booking.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                try {
                    sendReminderForBooking(booking);
                    booking.setReminderSent(true);
                    bookingRepository.save(booking);
                    log.info("✅ Reminder successfully sent for booking: {}", booking.getId());
                } catch (Exception e) {
                    log.error("❌ Failed to send reminder for booking {}: {}", booking.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error in reminder scheduler: {}", e.getMessage(), e);
        }
    }

    private void sendReminderForBooking(Booking booking) {
        log.info("Sending reminder for booking: {}", booking.getId());

        log.info("Fetching room info for roomId: {}", booking.getRoomId());
        RoomResponse room = referenceServiceClient.getRoom(booking.getRoomId());
        if (room == null) {
            log.warn("❌ Room not found for booking: {}", booking.getRoomId());
            return;
        }
        log.info("✅ Room found: {} ({})", room.getName(), room.getRoomNumber());

        List<String> allParticipantIds = new ArrayList<>();
        allParticipantIds.add(booking.getUserId());
        if (booking.getParticipantIds() != null && !booking.getParticipantIds().isEmpty()) {
            allParticipantIds.addAll(booking.getParticipantIds());
        }

        // Удаляем дубликаты
        allParticipantIds = allParticipantIds.stream().distinct().toList();

        log.info("Total participants for reminder: {}", allParticipantIds.size());
        log.info("Participants: {}", allParticipantIds);

        for (String participantId : allParticipantIds) {
            log.info("Sending reminder to participant: {}", participantId);
            sendReminderToParticipant(participantId, booking, room);
        }
    }

    private void sendReminderToParticipant(String participantId, Booking booking, RoomResponse room) {
        try {
            log.info("Fetching participant info for userId: {}", participantId);
            EmployeeResponse participant = referenceServiceClient.getEmployee(participantId);
            if (participant == null) {
                log.warn("❌ Participant not found: {}", participantId);
                return;
            }
            log.info("✅ Participant found: {} {}, email: {}",
                    participant.getFirstName(),
                    participant.getLastName(),
                    participant.getEmail());

            Map<String, Object> templateVars = new HashMap<>();
            templateVars.put("bookingId", booking.getId());
            templateVars.put("title", booking.getTitle());
            templateVars.put("roomName", room.getName());
            templateVars.put("roomNumber", room.getRoomNumber());
            templateVars.put("startTime", formatDateTime(booking.getStartTime()));
            templateVars.put("endTime", formatDateTime(booking.getEndTime()));
            templateVars.put("minutesBefore", reminderMinutesBefore);
            templateVars.put("userName", participant.getFirstName() + " " + participant.getLastName());

            if (participantId.equals(booking.getUserId())) {
                templateVars.put("isOrganizer", true);
                log.info("Participant {} is the organizer", participantId);
            } else {
                templateVars.put("isOrganizer", false);
                log.info("Fetching organizer info for userId: {}", booking.getUserId());
                EmployeeResponse organizer = referenceServiceClient.getEmployee(booking.getUserId());
                if (organizer != null) {
                    templateVars.put("organizerName", organizer.getFirstName() + " " + organizer.getLastName());
                    log.info("Organizer: {} {}", organizer.getFirstName(), organizer.getLastName());
                }
            }

            log.info("Building notification event for participant: {}", participant.getEmail());
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("BOOKING_REMINDER")
                    .source("booking-service")
                    .userId(participantId)
                    .recipientIds(List.of(participantId))
                    .templateCode("booking.reminder")
                    .templateVariables(templateVars)
                    .channels(List.of("EMAIL", "TELEGRAM"))
                    .priority(9)
                    .build();

            log.info("Publishing notification event to Kafka...");
            eventPublisher.publishNotificationEvent(notificationEvent);
            log.info("✅ Reminder sent to participant: {} ({})", participant.getEmail(), participantId);

        } catch (Exception e) {
            log.error("❌ Failed to send reminder to participant {}: {}", participantId, e.getMessage(), e);
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown time";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}