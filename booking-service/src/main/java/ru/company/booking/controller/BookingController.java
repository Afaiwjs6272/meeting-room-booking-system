package ru.company.booking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.company.booking.dto.BookingResponse;
import ru.company.booking.dto.CreateBookingRequest;
import ru.company.booking.dto.UpdateBookingRequest;
import ru.company.booking.service.BookingService;
import ru.company.common.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "APIs for managing meeting room bookings")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBooking(id)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bookings by user")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getUserBookings(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getUserBookings(userId, PageRequest.of(page, size))));
    }

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Booking created", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing booking")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable String id,
            @Valid @RequestBody UpdateBookingRequest request) {
        BookingResponse response = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(ApiResponse.success("Booking updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable String id,
            @RequestParam String userId,
            @RequestParam(required = false) String reason) {
        bookingService.cancelBooking(id, userId, reason);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled", null));
    }
}