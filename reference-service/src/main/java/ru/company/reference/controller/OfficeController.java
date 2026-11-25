package ru.company.reference.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.company.common.response.ApiResponse;
import ru.company.reference.dto.CreateOfficeRequest;
import ru.company.reference.dto.OfficeResponse;
import ru.company.reference.dto.UpdateOfficeRequest;
import ru.company.reference.service.OfficeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offices")
@RequiredArgsConstructor
@Tag(name = "Office Management", description = "APIs for managing office reference data")
public class OfficeController {

    private final OfficeService officeService;

    @GetMapping("/{id}")
    @Operation(summary = "Get office by ID")
    public ResponseEntity<ApiResponse<OfficeResponse>> getOffice(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(officeService.getOffice(id)));
    }

    @GetMapping("/by-code/{code}")
    @Operation(summary = "Get office by code")
    public ResponseEntity<ApiResponse<OfficeResponse>> getOfficeByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(officeService.getOfficeByCode(code)));
    }

    @GetMapping
    @Operation(summary = "Get all offices")
    public ResponseEntity<ApiResponse<List<OfficeResponse>>> getAllOffices() {
        return ResponseEntity.ok(ApiResponse.success(officeService.getAllOffices()));
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Get offices by city")
    public ResponseEntity<ApiResponse<List<OfficeResponse>>> getOfficesByCity(@PathVariable String city) {
        return ResponseEntity.ok(ApiResponse.success(officeService.getOfficesByCity(city)));
    }

    @PostMapping
    @Operation(summary = "Create a new office")
    public ResponseEntity<ApiResponse<OfficeResponse>> createOffice(@Valid @RequestBody CreateOfficeRequest request) {
        OfficeResponse response = officeService.createOffice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Office created", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing office")
    public ResponseEntity<ApiResponse<OfficeResponse>> updateOffice(
            @PathVariable String id,
            @Valid @RequestBody UpdateOfficeRequest request) {
        OfficeResponse response = officeService.updateOffice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Office updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an office")
    public ResponseEntity<ApiResponse<Void>> deleteOffice(@PathVariable String id) {
        officeService.deleteOffice(id);
        return ResponseEntity.ok(ApiResponse.success("Office deleted", null));
    }
}