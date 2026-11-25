package ru.company.notification.client;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import ru.company.common.reference.EmployeeResponse;
import ru.company.common.response.ApiResponse;
import ru.company.notification.config.FeignConfig;

import java.util.Map;

@FeignClient(name = "reference-service", url = "${reference.service.url:http://localhost:8081}",
        configuration = FeignConfig.class)
public interface ReferenceServiceClient {

    @GetMapping("/api/v1/employees/{id}")
    ApiResponse<EmployeeResponse> getEmployee(@PathVariable("id") String id);
    @PutMapping("/api/v1/employees/{id}")
    ApiResponse<EmployeeResponse> updateEmployee(@PathVariable("id") String id, @RequestBody Map<String, String> updates);
}
