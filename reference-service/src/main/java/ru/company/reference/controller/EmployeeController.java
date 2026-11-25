package ru.company.reference.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.company.common.response.ApiResponse;
import ru.company.reference.dto.CreateEmployeeRequest;
import ru.company.reference.dto.EmployeeResponse;
import ru.company.reference.dto.UpdateEmployeeRequest;
import ru.company.reference.service.EmployeeService;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing employee reference data")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployee(id)));
    }

    @GetMapping
    @Operation(summary = "Get all employees with pagination")
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String userId) {

        String currentUserId = userId != null ? userId : "system";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName"));
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees(pageable, currentUserId)));
    }

    @PostMapping
    @Operation(summary = "Create a new employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request,
            @RequestParam(required = false) String userId) {

        String currentUserId = userId != null ? userId : "system";
        EmployeeResponse response = employeeService.createEmployee(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Employee created", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable String id,
            @Valid @RequestBody UpdateEmployeeRequest request,
            @RequestParam(required = false) String userId) {

        String currentUserId = userId != null ? userId : "system";
        EmployeeResponse response = employeeService.updateEmployee(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Employee updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @PathVariable String id,
            @RequestParam(required = false) String userId) {

        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted", null));
    }
}