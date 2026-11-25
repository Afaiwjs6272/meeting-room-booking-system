package ru.company.integration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.company.common.reference.EmployeeResponse;

import java.util.List;

@FeignClient(name = "reference-service", url = "${reference.service.url:http://localhost:8081}")
public interface ReferenceServiceClient {

    @GetMapping("/api/v1/employees")
    List<EmployeeResponse> getAllEmployees();

    @PostMapping("/api/v1/employees")
    EmployeeResponse createEmployee(@RequestBody Object employee);

    @GetMapping("/api/v1/employees/external/{externalId}")
    EmployeeResponse getEmployeeByExternalId(@PathVariable("externalId") String externalId);
}