package ru.company.reference.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rooms")
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Room extends BaseEntity {

    @Column(name = "room_number", nullable = false, unique = true)
    private String roomNumber;

    @Column(nullable = false)
    private String name;

    private Integer floor;

    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Enumerated(EnumType.STRING)
    private RoomType type;

    @ManyToMany
    @JoinTable(
            name = "room_equipment",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private Set<Equipment> equipment = new HashSet<>();

    @Column(name = "has_projector")
    private Boolean hasProjector;

    @Column(name = "has_whiteboard")
    private Boolean hasWhiteboard;

    @Column(name = "has_tv_screen")
    private Boolean hasTVScreen;

    @Column(name = "has_video_conference")
    private Boolean hasVideoConference;

    private String description;

    @Column(name = "is_accessible")
    private Boolean isAccessible = true;

    @Column(name = "requires_approval")
    private Boolean requiresApproval = false;

    @Column(name = "approver_employee_id")
    private String approverEmployeeId;
}