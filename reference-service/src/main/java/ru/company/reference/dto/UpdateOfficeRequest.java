package ru.company.reference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOfficeRequest {
    private String id;
    private String code;
    private String name;
    private String address;
    private String city;
    private String country;
    private String timezone;
    private LocalTime workStartTime;
    private LocalTime workEndTime;
    private Integer maxBookingDurationHours;
    private Integer minAdvanceBookingMinutes;
    private Integer maxAdvanceBookingDays;
    private Double latitude;
    private Double longitude;
    private Boolean active;
}