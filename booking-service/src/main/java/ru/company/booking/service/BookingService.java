package ru.company.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.company.booking.client.ReferenceServiceClient;
import ru.company.booking.dto.BookingResponse;
import ru.company.booking.dto.CreateBookingRequest;
import ru.company.booking.dto.UpdateBookingRequest;
import ru.company.booking.entity.Booking;
import ru.company.booking.entity.BookingStatus;
import ru.company.booking.event.EventPublisher;
import ru.company.booking.repository.BookingRepository;
import ru.company.common.event.BookingEvent;
import ru.company.common.event.NotificationEvent;
import ru.company.common.exception.BusinessException;
import ru.company.common.reference.EmployeeResponse;
import ru.company.common.reference.RoomResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ReferenceServiceClient referenceServiceClient;
    private final EventPublisher eventPublisher;

    public BookingResponse getBooking(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Booking not found"));
        return mapToResponse(booking);
    }

    public Page<BookingResponse> getUserBookings(String userId, Pageable pageable) {
        return bookingRepository.findByUserIdAndStatusNot(userId, BookingStatus.CANCELLED, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        log.info("Creating booking for room: {}, user: {}", request.getRoomId(), request.getUserId());

        RoomResponse room = referenceServiceClient.getRoom(request.getRoomId());
        if (room == null) {
            throw new BusinessException("Room not found");
        }

        EmployeeResponse user = referenceServiceClient.getEmployee(request.getUserId());
        if (user == null) {
            throw new BusinessException("User not found");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                request.getRoomId(), request.getStartTime(), request.getEndTime());
        if (!overlapping.isEmpty()) {
            throw new BusinessException("Room is not available at requested time");
        }

        Booking booking = Booking.builder()
                .roomId(request.getRoomId())
                .userId(request.getUserId())
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .participantIds(request.getParticipantIds() != null ? request.getParticipantIds() : new ArrayList<>())
                .status(BookingStatus.CONFIRMED)
                .reminderSent(false)
                .build();

        booking = bookingRepository.save(booking);
        log.info("Booking saved with id: {}, reminder_sent: {}", booking.getId(), booking.getReminderSent());

        publishBookingCreatedEvent(booking, room, user);

        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse updateBooking(String id, UpdateBookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Cannot update cancelled booking");
        }

        // Check availability if times changed
        if (request.getStartTime() != null && request.getEndTime() != null) {
            List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                    booking.getRoomId(), request.getStartTime(), request.getEndTime());

            Booking finalBooking = booking;
            boolean hasConflict = overlapping.stream()
                    .anyMatch(b -> !b.getId().equals(finalBooking.getId()));

            if (hasConflict) {
                throw new BusinessException("Room is not available at requested time");
            }

            booking.setStartTime(request.getStartTime());
            booking.setEndTime(request.getEndTime());
        }

        if (request.getTitle() != null) booking.setTitle(request.getTitle());
        if (request.getDescription() != null) booking.setDescription(request.getDescription());
        if (request.getParticipantIds() != null) booking.setParticipantIds(request.getParticipantIds());

        booking = bookingRepository.save(booking);

        // Publish update event
        publishBookingUpdatedEvent(booking);

        return mapToResponse(booking);
    }

    @Transactional
    public void cancelBooking(String id, String userId, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelledBy(userId);

        bookingRepository.save(booking);

        // Publish cancellation event
        publishBookingCancelledEvent(booking, reason);
    }

    private void publishBookingCreatedEvent(Booking booking, RoomResponse room, EmployeeResponse user) {
        // Publish booking event
        BookingEvent bookingEvent = BookingEvent.builder()
                .eventType("BOOKING_CREATED")
                .source("booking-service")
                .bookingId(booking.getId())
                .roomId(booking.getRoomId())
                .userId(booking.getUserId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .title(booking.getTitle())
                .action(BookingEvent.BookingAction.CREATE)
                .build();

        eventPublisher.publishBookingEvent(bookingEvent);

        // Format dates for email
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String formattedStartTime = booking.getStartTime().format(formatter);
        String formattedEndTime = booking.getEndTime().format(formatter);

        // Publish notification event for organizer
        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put("bookingId", booking.getId());
        templateVars.put("roomName", room.getName());
        templateVars.put("roomNumber", room.getRoomNumber());
        templateVars.put("startTime", formattedStartTime);
        templateVars.put("endTime", formattedEndTime);
        templateVars.put("title", booking.getTitle());
        templateVars.put("userName", user.getFirstName() + " " + user.getLastName());

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .eventType("BOOKING_CONFIRMATION")
                .source("booking-service")
                .userId(booking.getUserId())
                .recipientIds(List.of(booking.getUserId()))
                .templateCode("booking.created")
                .templateVariables(templateVars)
                .channels(List.of("EMAIL"))
                .priority(8)
                .build();

        eventPublisher.publishNotificationEvent(notificationEvent);

        // Also send notifications to participants
        if (booking.getParticipantIds() != null && !booking.getParticipantIds().isEmpty()) {
            for (String participantId : booking.getParticipantIds()) {
                if (!participantId.equals(booking.getUserId())) {
                    NotificationEvent participantEvent = NotificationEvent.builder()
                            .eventType("BOOKING_CONFIRMATION")
                            .source("booking-service")
                            .userId(participantId)
                            .recipientIds(List.of(participantId))
                            .templateCode("booking.created")
                            .templateVariables(templateVars)
                            .channels(List.of("EMAIL"))
                            .priority(8)
                            .build();
                    eventPublisher.publishNotificationEvent(participantEvent);
                }
            }
        }
    }

    private void publishBookingUpdatedEvent(Booking booking) {
        BookingEvent event = BookingEvent.builder()
                .eventType("BOOKING_MODIFIED")
                .source("booking-service")
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .title(booking.getTitle())
                .action(BookingEvent.BookingAction.UPDATE)
                .build();

        eventPublisher.publishBookingEvent(event);
    }

    private void publishBookingCancelledEvent(Booking booking, String reason) {
        BookingEvent event = BookingEvent.builder()
                .eventType("BOOKING_CANCELLED")
                .source("booking-service")
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .action(BookingEvent.BookingAction.CANCEL)
                .build();

        eventPublisher.publishBookingEvent(event);

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .eventType("BOOKING_CANCELLED")
                .source("booking-service")
                .userId(booking.getUserId())
                .recipientIds(List.of(booking.getUserId()))
                .templateCode("booking.cancelled")
                .templateVariables(Map.of(
                        "bookingId", booking.getId(),
                        "title", booking.getTitle(),
                        "reason", reason
                ))
                .channels(List.of("EMAIL"))
                .priority(7)
                .build();

        eventPublisher.publishNotificationEvent(notificationEvent);
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .roomId(booking.getRoomId())
                .userId(booking.getUserId())
                .title(booking.getTitle())
                .description(booking.getDescription())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus().name())
                .participantIds(booking.getParticipantIds())
                .createdAt(booking.getCreatedAt())
                .reminderSent(booking.getReminderSent())
                .build();
    }
}