package ru.company.common.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private String id;
    private String roomNumber;
    private String name;
    private Integer floor;
    private Integer capacity;
    private OfficeInfo office;
    private String type;
    private Set<EquipmentInfo> equipment;
    private Boolean hasProjector;
    private Boolean hasWhiteboard;
    private Boolean hasTVScreen;
    private Boolean hasVideoConference;
    private String description;
    private Boolean isAccessible;
    private Boolean requiresApproval;
    private Boolean active;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfficeInfo {
        private String id;
        private String code;
        private String name;
        private String timezone;
        private LocalTime workStartTime;
        private LocalTime workEndTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipmentInfo {
        private String id;
        private String name;
        private String type;
    }
}