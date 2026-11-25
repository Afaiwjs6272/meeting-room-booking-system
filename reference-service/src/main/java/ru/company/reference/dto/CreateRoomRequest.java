package ru.company.reference.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotBlank(message = "Room name is required")
    private String name;

    private Integer floor;
    private Integer capacity;

    @NotBlank(message = "Office ID is required")
    private String officeId;

    private String type;
    private Set<String> equipmentIds;
    private Boolean hasProjector;
    private Boolean hasWhiteboard;
    private Boolean hasTVScreen;
    private Boolean hasVideoConference;
    private String description;
}