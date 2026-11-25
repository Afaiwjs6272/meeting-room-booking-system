package ru.company.reference.mapper;

import org.springframework.stereotype.Component;
import ru.company.reference.dto.CreateOfficeRequest;
import ru.company.reference.dto.OfficeResponse;
import ru.company.reference.dto.UpdateOfficeRequest;
import ru.company.reference.entity.Office;

@Component
public class OfficeMapper {

    public OfficeResponse toResponse(Office office) {
        if (office == null) return null;

        return OfficeResponse.builder()
                .id(office.getId())
                .code(office.getCode())
                .name(office.getName())
                .address(office.getAddress())
                .city(office.getCity())
                .country(office.getCountry())
                .timezone(office.getTimezone())
                .workStartTime(office.getWorkStartTime())
                .workEndTime(office.getWorkEndTime())
                .maxBookingDurationHours(office.getMaxBookingDurationHours())
                .active(office.getActive())
                .createdAt(office.getCreatedAt())
                .updatedAt(office.getUpdatedAt())
                .build();
    }

    public Office toEntity(CreateOfficeRequest request) {
        if (request == null) return null;

        return Office.builder()
                .code(request.getCode())
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .timezone(request.getTimezone())
                .workStartTime(request.getWorkStartTime())
                .workEndTime(request.getWorkEndTime())
                .maxBookingDurationHours(request.getMaxBookingDurationHours())
                .active(true)
                .build();
    }

    public void updateEntity(Office office, UpdateOfficeRequest request) {
        if (request.getCode() != null) office.setCode(request.getCode());
        if (request.getName() != null) office.setName(request.getName());
        if (request.getAddress() != null) office.setAddress(request.getAddress());
        if (request.getCity() != null) office.setCity(request.getCity());
        if (request.getCountry() != null) office.setCountry(request.getCountry());
        if (request.getTimezone() != null) office.setTimezone(request.getTimezone());
        if (request.getWorkStartTime() != null) office.setWorkStartTime(request.getWorkStartTime());
        if (request.getWorkEndTime() != null) office.setWorkEndTime(request.getWorkEndTime());
        if (request.getMaxBookingDurationHours() != null)
            office.setMaxBookingDurationHours(request.getMaxBookingDurationHours());
        if (request.getActive() != null) office.setActive(request.getActive());
    }
}