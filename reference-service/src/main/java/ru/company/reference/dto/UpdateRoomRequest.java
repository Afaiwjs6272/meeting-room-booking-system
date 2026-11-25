package ru.company.reference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomRequest {
    private String id;
    private String roomNumber;
    private String name;
    private Integer floor;
    private Integer capacity;
    private String officeId;
    private String type;
    private Set<String> equipmentIds;
    private Boolean hasProjector;
    private Boolean hasWhiteboard;
    private Boolean hasTVScreen;
    private Boolean hasVideoConference;
    private String description;
    private Boolean isAccessible;
    private Boolean requiresApproval;
    private String approverEmployeeId;
    private Boolean active;
}