package ru.company.reference.mapper;

import org.springframework.stereotype.Component;
import ru.company.reference.dto.CreateRoomRequest;
import ru.company.reference.dto.RoomResponse;
import ru.company.reference.dto.UpdateRoomRequest;
import ru.company.reference.entity.Office;
import ru.company.reference.entity.Room;
import ru.company.reference.entity.RoomType;

@Component
public class RoomMapper {

    public RoomResponse toResponse(Room room) {
        if (room == null) return null;

        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .name(room.getName())
                .floor(room.getFloor())
                .capacity(room.getCapacity())
                .office(toOfficeInfo(room.getOffice()))
                .type(room.getType() != null ? room.getType().name() : null)
                .hasProjector(room.getHasProjector())
                .hasWhiteboard(room.getHasWhiteboard())
                .hasTVScreen(room.getHasTVScreen())
                .hasVideoConference(room.getHasVideoConference())
                .description(room.getDescription())
                .active(room.getActive())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    public Room toEntity(CreateRoomRequest request, Office office) {
        if (request == null) return null;

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .name(request.getName())
                .floor(request.getFloor())
                .capacity(request.getCapacity())
                .office(office)
                .hasProjector(request.getHasProjector())
                .hasWhiteboard(request.getHasWhiteboard())
                .hasTVScreen(request.getHasTVScreen())
                .hasVideoConference(request.getHasVideoConference())
                .description(request.getDescription())
                .isAccessible(true)
                .requiresApproval(false)
                .active(true)
                .build();

        if (request.getType() != null) {
            try {
                room.setType(RoomType.valueOf(request.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                room.setType(RoomType.MEETING);
            }
        } else {
            room.setType(RoomType.MEETING);
        }

        return room;
    }

    public void updateEntity(Room room, UpdateRoomRequest request) {
        if (request == null) return;

        if (request.getRoomNumber() != null) room.setRoomNumber(request.getRoomNumber());
        if (request.getName() != null) room.setName(request.getName());
        if (request.getFloor() != null) room.setFloor(request.getFloor());
        if (request.getCapacity() != null) room.setCapacity(request.getCapacity());
        if (request.getHasProjector() != null) room.setHasProjector(request.getHasProjector());
        if (request.getHasWhiteboard() != null) room.setHasWhiteboard(request.getHasWhiteboard());
        if (request.getHasTVScreen() != null) room.setHasTVScreen(request.getHasTVScreen());
        if (request.getHasVideoConference() != null) room.setHasVideoConference(request.getHasVideoConference());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getActive() != null) room.setActive(request.getActive());

        if (request.getType() != null) {
            try {
                room.setType(RoomType.valueOf(request.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // keep existing type
            }
        }
    }

    private RoomResponse.OfficeInfo toOfficeInfo(Office office) {
        if (office == null) return null;
        return RoomResponse.OfficeInfo.builder()
                .id(office.getId())
                .code(office.getCode())
                .name(office.getName())
                .city(office.getCity())
                .build();
    }
}