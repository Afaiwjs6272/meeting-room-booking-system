package ru.company.booking.client;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.company.common.reference.EmployeeResponse;
import ru.company.common.reference.RoomResponse;

@Component
@Slf4j
public class ReferenceServiceFallbackFactory implements FallbackFactory<ReferenceServiceClient> {

    @Override
    public ReferenceServiceClient create(Throwable cause) {
        return new ReferenceServiceClient() {

            @Override
            public EmployeeResponse getEmployee(String id) {
                log.error("Failed to get employee {}: {}", id, cause.getMessage());
                if (cause instanceof FeignException && ((FeignException) cause).status() == 404) {
                    return null;
                }
                return EmployeeResponse.builder()
                        .id(id)
                        .firstName("Unknown")
                        .lastName("User")
                        .email("unknown@company.com")
                        .build();
            }

            @Override
            public RoomResponse getRoom(String id) {
                log.error("Failed to get room {}: {}", id, cause.getMessage());
                return RoomResponse.builder()
                        .id(id)
                        .name("Unknown Room")
                        .roomNumber("N/A")
                        .build();
            }
        };
    }
}