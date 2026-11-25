package ru.company.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.company.booking.dto.BookingResponse;
import ru.company.booking.dto.CreateBookingRequest;
import ru.company.booking.dto.UpdateBookingRequest;
import ru.company.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        if (!objectMapper.getRegisteredModuleIds().contains(new JavaTimeModule().getTypeId())) {
            objectMapper.registerModule(new JavaTimeModule());
        }
    }

    private BookingResponse sampleResponse() {
        return BookingResponse.builder()
                .id("b-1")
                .roomId("r-1")
                .userId("u-1")
                .title("Meeting")
                .description("desc")
                .startTime(LocalDateTime.of(2030, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2030, 1, 1, 11, 0))
                .status("CONFIRMED")
                .participantIds(List.of("u-2"))
                .reminderSent(false)
                .build();
    }

    @Test
    void getBooking_returnsOk() throws Exception {
        when(bookingService.getBooking("b-1")).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/bookings/b-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("b-1"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void getUserBookings_returnsPage() throws Exception {
        Page<BookingResponse> page = new PageImpl<>(List.of(sampleResponse()));
        when(bookingService.getUserBookings(eq("u-1"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/bookings/user/u-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value("b-1"));
    }

    @Test
    void createBooking_valid_returnsCreated() throws Exception {
        CreateBookingRequest req = CreateBookingRequest.builder()
                .roomId("r-1").userId("u-1").title("Meeting")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();
        when(bookingService.createBooking(any(CreateBookingRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Booking created"))
                .andExpect(jsonPath("$.data.id").value("b-1"));
    }

    @Test
    void createBooking_missingFields_returnsBadRequest() throws Exception {
        CreateBookingRequest req = CreateBookingRequest.builder()
                // missing roomId, userId, title, start/end
                .description("desc")
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_pastStartTime_returnsBadRequest() throws Exception {
        CreateBookingRequest req = CreateBookingRequest.builder()
                .roomId("r-1").userId("u-1").title("T")
                .startTime(LocalDateTime.of(2000, 1, 1, 10, 0))
                .endTime(LocalDateTime.of(2000, 1, 1, 11, 0))
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBooking_returnsOk() throws Exception {
        UpdateBookingRequest req = UpdateBookingRequest.builder().title("New").build();
        when(bookingService.updateBooking(eq("b-1"), any(UpdateBookingRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/bookings/b-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Booking updated"));
    }

    @Test
    void cancelBooking_callsService_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/bookings/b-1")
                        .param("userId", "u-1")
                        .param("reason", "Meeting moved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Booking cancelled"));
        verify(bookingService).cancelBooking("b-1", "u-1", "Meeting moved");
    }

    @Test
    void cancelBooking_withoutReason_callsServiceWithNullReason() throws Exception {
        mockMvc.perform(delete("/api/v1/bookings/b-1").param("userId", "u-1"))
                .andExpect(status().isOk());
        verify(bookingService).cancelBooking(eq("b-1"), eq("u-1"), org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    void cancelBooking_missingUserId_returnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/bookings/b-1"))
                .andExpect(status().isBadRequest());
    }
}
