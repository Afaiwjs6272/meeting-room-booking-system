package ru.company.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOfficeRequest {

    @NotBlank(message = "Office code is required")
    private String code;

    @NotBlank(message = "Office name is required")
    private String name;

    private String address;
    private String city;
    private String country;
    private String timezone;

    @NotNull(message = "Work start time is required")
    private LocalTime workStartTime;

    @NotNull(message = "Work end time is required")
    private LocalTime workEndTime;

    private Integer maxBookingDurationHours;
}