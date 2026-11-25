package ru.company.reference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfficeInfo {
        private String id;
        private String code;
        private String name;
        private String city;
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