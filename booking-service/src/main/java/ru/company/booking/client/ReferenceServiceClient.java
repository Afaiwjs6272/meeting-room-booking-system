package ru.company.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.company.booking.config.InternalRequestInterceptor;
import ru.company.common.reference.EmployeeResponse;
import ru.company.common.reference.RoomResponse;

@FeignClient(name = "reference-service", url = "${reference.service.url:http://localhost:8081}",
        fallbackFactory = ReferenceServiceFallbackFactory.class,
        configuration = InternalRequestInterceptor.class)
public interface ReferenceServiceClient {

    @GetMapping("/api/v1/employees/{id}")
    EmployeeResponse getEmployee(@PathVariable("id") String id);

    @GetMapping("/api/v1/rooms/{id}")
    RoomResponse getRoom(@PathVariable("id") String id);
}