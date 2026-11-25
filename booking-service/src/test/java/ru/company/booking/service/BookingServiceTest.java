package ru.company.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReferenceServiceClient referenceServiceClient;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private BookingService bookingService;

    private RoomResponse sampleRoom() {
        return RoomResponse.builder()
                .id("r-1").roomNumber("101").name("Alpha").build();
    }

    private EmployeeResponse sampleUser(String id) {
        return EmployeeResponse.builder()
                .id(id).firstName("John").lastName("Doe").email(id + "@c.com").build();
    }

    private Booking sampleBooking(String id) {
        return Booking.builder()
                .id(id)
                .roomId("r-1")
                .userId("u-1")
                .title("Meeting")
                .description("desc")
                .startTime(LocalDateTime.of(2030, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2030, 1, 1, 11, 0))
                .participantIds(new ArrayList<>(List.of("u-2", "u-3")))
                .status(BookingStatus.CONFIRMED)
                .reminderSent(false)
                .build();
    }

    // ===== getBooking =====

    @Test
    void getBooking_found_returnsMappedResponse() {
        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(sampleBooking("b-1")));

        BookingResponse resp = bookingService.getBooking("b-1");

        assertThat(resp.getId()).isEqualTo("b-1");
        assertThat(resp.getRoomId()).isEqualTo("r-1");
        assertThat(resp.getUserId()).isEqualTo("u-1");
        assertThat(resp.getTitle()).isEqualTo("Meeting");
        assertThat(resp.getStatus()).isEqualTo("CONFIRMED");
        assertThat(resp.getParticipantIds()).containsExactly("u-2", "u-3");
        assertThat(resp.getReminderSent()).isFalse();
    }

    @Test
    void getBooking_notFound_throwsBusinessException() {
        when(bookingRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBooking("missing"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Booking not found");
    }

    // ===== getUserBookings =====

    @Test
    void getUserBookings_returnsPageExcludingCancelled() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> page = new PageImpl<>(List.of(sampleBooking("b-1")), pageable, 1);
        when(bookingRepository.findByUserIdAndStatusNot("u-1", BookingStatus.CANCELLED, pageable))
                .thenReturn(page);

        Page<BookingResponse> result = bookingService.getUserBookings("u-1", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("b-1");
    }

    // ===== createBooking =====

    @Test
    void createBooking_success_savesAndPublishesEvents() {
        CreateBookingRequest req = CreateBookingRequest.builder()
                .roomId("r-1")
                .userId("u-1")
                .title("Meeting")
                .description("desc")
                .startTime(LocalDateTime.of(2030, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2030, 1, 1, 11, 0))
                .participantIds(List.of("u-2", "u-3"))
                .build();

        when(referenceServiceClient.getRoom("r-1")).thenReturn(sampleRoom());
        when(referenceServiceClient.getEmployee("u-1")).thenReturn(sampleUser("u-1"));
        when(bookingRepository.findOverlappingBookings(eq("r-1"), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId("new-b");
            return b;
        });

        BookingResponse resp = bookingService.createBooking(req);

        assertThat(resp.getId()).isEqualTo("new-b");
        assertThat(resp.getStatus()).isEqualTo("CONFIRMED");

        ArgumentCaptor<Booking> savedCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(savedCaptor.capture());
        Booking saved = savedCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(saved.getReminderSent()).isFalse();
        assertThat(saved.getParticipantIds()).containsExactly("u-2", "u-3");

        // 1 booking event + 1 notification to organizer + 2 notifications for participants
        verify(eventPublisher).publishBookingEvent(any(BookingEvent.class));
        verify(eventPublisher, times(3)).publishNotificationEvent(any(NotificationEvent.class));
    }

    @Test
    void createBooking_noParticipants_publishesOnlyOrganizerNotification() {
        CreateBookingRequest req = CreateBookingRequest.builder()
                .roomId("r-1").userId("u-1").title("T")
                .startTime(LocalDateTime.of(2030, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2030, 1, 1, 11, 0))
                .build();
        when(referenceServiceClient.getRoom("r-1")).thenReturn(sampleRoom());
        when(referenceServiceClient.getEmployee("u-1")).thenReturn(sampleUser("u-1"));
        when(bookingRepository.findOverlappingBookings(anyString(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId("b-new");
            return b;
        });

        bookingService.createBooking(req);

        verify(eventPublisher, times(1)).publishNotificationEvent(any(NotificationEvent.class));
    }

    @Test
    void createBooking_participantIsOrganizer_skipsDuplicateNotification() {
        CreateBookingRequest req = CreateBookingRequest.builder()
                .roomId("r-1").userId("u-1").title("T")
                .startTime(LocalDateTime.of(2030, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2030, 1, 1, 11, 0))
                .participantIds(List.of("u-1", "u-2")) // includes organizer
                .build();
        when(referenceServiceClient.getRoom("r-1")).thenReturn(sampleRoom());
        when(referenceServiceClient.getEmployee("u-1")).thenReturn(sampleUser("u-1"));
        when(bookingRepository.findOverlappingBookings(anyString(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId("b-new");
            return b;
        });

        bookingService.createBooking(req);

        // organizer (1) + unique participant u-2 (1) = 2 notifications
        verify(eventPublisher, times(2)).publishNotificationEvent(any(NotificationEvent.class));
    }

    @Test
    void createBooking_roomNotFound_throwsBusinessException() {
        CreateBookingRequest req = CreateBookingRequest.builder()
                .roomId("r-1").userId("u-1").title("T")
                .startTime(LocalDateTime.of(2030, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2030, 1, 1, 11, 0))
                .build();
        when(referenceServiceClient.getRoom("r-1")).thenReturn(null);

        assertThatThrownBy(() -> bookingService.createBooking(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Room not found");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_userNotFound_throwsBusinessException() {
        CreateBookingRequest req = CreateBookingRequest.builder()
                .roomId("r-1").userId("u-1").title("T")
                .startTime(LocalDateTime.of(2030, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2030, 1, 1, 11, 0))
                .build();
        when(referenceServiceClient.getRoom("r-1")).thenReturn(sampleRoom());
        when(referenceServiceClient.getEmployee("u-1")).thenReturn(null);

        assertThatThrownBy(() -> bookingService.createBooking(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createBooking_overlappingBookings_throwsBusinessException() {
        CreateBookingRequest req = CreateBookingRequest.builder()
                .roomId("r-1").userId("u-1").title("T")
                .startTime(LocalDateTime.of(2030, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2030, 1, 1, 11, 0))
                .build();
        when(referenceServiceClient.getRoom("r-1")).thenReturn(sampleRoom());
        when(referenceServiceClient.getEmployee("u-1")).thenReturn(sampleUser("u-1"));
        when(bookingRepository.findOverlappingBookings(anyString(), any(), any()))
                .thenReturn(List.of(sampleBooking("existing")));

        assertThatThrownBy(() -> bookingService.createBooking(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");
        verify(bookingRepository, never()).save(any());
    }

    // ===== updateBooking =====

    @Test
    void updateBooking_updatesFieldsAndPublishesEvent() {
        Booking existing = sampleBooking("b-1");
        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookingRequest req = UpdateBookingRequest.builder()
                .title("New title")
                .description("new desc")
                .build();

        BookingResponse resp = bookingService.updateBooking("b-1", req);

        assertThat(resp.getTitle()).isEqualTo("New title");
        assertThat(resp.getDescription()).isEqualTo("new desc");
        verify(eventPublisher).publishBookingEvent(any(BookingEvent.class));
    }

    @Test
    void updateBooking_withNewTimes_updatesAndChecksOverlap() {
        Booking existing = sampleBooking("b-1");
        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(existing));
        LocalDateTime newStart = LocalDateTime.of(2030, 1, 1, 12, 0);
        LocalDateTime newEnd = LocalDateTime.of(2030, 1, 1, 13, 0);
        // overlap returns only the same booking -> not a conflict
        when(bookingRepository.findOverlappingBookings(eq("r-1"), eq(newStart), eq(newEnd)))
                .thenReturn(List.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookingRequest req = UpdateBookingRequest.builder()
                .startTime(newStart).endTime(newEnd).build();

        BookingResponse resp = bookingService.updateBooking("b-1", req);

        assertThat(resp.getStartTime()).isEqualTo(newStart);
        assertThat(resp.getEndTime()).isEqualTo(newEnd);
    }

    @Test
    void updateBooking_overlapsWithOther_throws() {
        Booking existing = sampleBooking("b-1");
        Booking other = sampleBooking("b-2");
        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(existing));
        LocalDateTime newStart = LocalDateTime.of(2030, 1, 1, 12, 0);
        LocalDateTime newEnd = LocalDateTime.of(2030, 1, 1, 13, 0);
        when(bookingRepository.findOverlappingBookings(anyString(), any(), any()))
                .thenReturn(List.of(existing, other));

        UpdateBookingRequest req = UpdateBookingRequest.builder()
                .startTime(newStart).endTime(newEnd).build();

        assertThatThrownBy(() -> bookingService.updateBooking("b-1", req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateBooking_notFound_throws() {
        when(bookingRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBooking("x", UpdateBookingRequest.builder().build()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void updateBooking_cancelledBooking_throws() {
        Booking existing = sampleBooking("b-1");
        existing.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookingService.updateBooking(
                "b-1", UpdateBookingRequest.builder().title("x").build()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    void updateBooking_participantIdsOnly_replacesList() {
        Booking existing = sampleBooking("b-1");
        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateBookingRequest req = UpdateBookingRequest.builder()
                .participantIds(List.of("u-99")).build();

        BookingResponse resp = bookingService.updateBooking("b-1", req);

        assertThat(resp.getParticipantIds()).containsExactly("u-99");
    }

    // ===== cancelBooking =====

    @Test
    void cancelBooking_marksCancelledAndPublishesEvents() {
        Booking existing = sampleBooking("b-1");
        when(bookingRepository.findById("b-1")).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        bookingService.cancelBooking("b-1", "u-1", "Meeting moved");

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(saved.getCancelledBy()).isEqualTo("u-1");
        assertThat(saved.getCancelledAt()).isNotNull();

        verify(eventPublisher, atLeastOnce()).publishBookingEvent(any(BookingEvent.class));
        verify(eventPublisher).publishNotificationEvent(any(NotificationEvent.class));
    }

    @Test
    void cancelBooking_notFound_throws() {
        when(bookingRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking("x", "u-1", "r"))
                .isInstanceOf(BusinessException.class);
        verify(eventPublisher, never()).publishBookingEvent(any());
    }
}
