package ru.company.reference.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.company.common.exception.BusinessException;
import ru.company.reference.dto.CreateRoomRequest;
import ru.company.reference.dto.RoomResponse;
import ru.company.reference.dto.UpdateRoomRequest;
import ru.company.reference.entity.Office;
import ru.company.reference.entity.Room;
import ru.company.reference.exception.ResourceNotFoundException;
import ru.company.reference.mapper.RoomMapper;
import ru.company.reference.repository.OfficeRepository;
import ru.company.reference.repository.RoomRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final OfficeRepository officeRepository;
    private final RoomMapper roomMapper;

    public RoomResponse getRoom(String id) {
        log.debug("Fetching room with id: {}", id);

        HttpServletRequest request = getCurrentHttpRequest();
        String internalHeader = request != null ? request.getHeader("X-Internal-Request") : null;
        boolean isInternal = "true".equalsIgnoreCase(internalHeader);

        if (!isInternal) {
            log.debug("External request for room - no security check needed");
        } else {
            log.debug("Internal request for room: {}", id);
        }

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        return roomMapper.toResponse(room);
    }

    public List<RoomResponse> getRoomsByOffice(String officeId) {
        return roomRepository.findByOfficeIdAndActiveTrue(officeId).stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<RoomResponse> getAvailableRooms(String officeId, Integer minCapacity) {
        return roomRepository.findAvailableRoomsInOffice(officeId, minCapacity).stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        log.info("Creating new room with number: {}", request.getRoomNumber());

        if (roomRepository.findByRoomNumber(request.getRoomNumber()).isPresent()) {
            throw new BusinessException("Room with number " + request.getRoomNumber() + " already exists");
        }

        Office office = officeRepository.findById(request.getOfficeId())
                .orElseThrow(() -> new ResourceNotFoundException("Office", "id", request.getOfficeId()));

        Room room = roomMapper.toEntity(request, office);
        Room saved = roomRepository.save(room);
        log.info("Room created with id: {}", saved.getId());

        return roomMapper.toResponse(saved);
    }

    @Transactional
    public RoomResponse updateRoom(String id, UpdateRoomRequest request) {
        log.info("Updating room with id: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        if (request.getOfficeId() != null) {
            Office office = officeRepository.findById(request.getOfficeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Office", "id", request.getOfficeId()));
            room.setOffice(office);
        }

        roomMapper.updateEntity(room, request);
        Room updated = roomRepository.save(room);

        return roomMapper.toResponse(updated);
    }

    @Transactional
    public void deleteRoom(String id) {
        log.warn("Deleting room with id: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        roomRepository.delete(room);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}