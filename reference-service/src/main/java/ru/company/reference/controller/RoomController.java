package ru.company.reference.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.company.common.response.ApiResponse;
import ru.company.reference.dto.CreateRoomRequest;
import ru.company.reference.dto.RoomResponse;
import ru.company.reference.dto.UpdateRoomRequest;
import ru.company.reference.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Room Management", description = "APIs for managing meeting rooms")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{id}")
    @Operation(summary = "Get room by ID")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoom(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(roomService.getRoom(id)));
    }

    @GetMapping("/office/{officeId}")
    @Operation(summary = "Get rooms by office")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByOffice(@PathVariable String officeId) {
        return ResponseEntity.ok(ApiResponse.success(roomService.getRoomsByOffice(officeId)));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available rooms")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailableRooms(
            @RequestParam String officeId,
            @RequestParam(required = false, defaultValue = "1") Integer minCapacity) {
        return ResponseEntity.ok(ApiResponse.success(roomService.getAvailableRooms(officeId, minCapacity)));
    }

    @PostMapping
    @Operation(summary = "Create a new room")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Room created", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing room")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoomRequest request) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Room updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a room")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable String id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Room deleted", null));
    }
}