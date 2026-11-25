package ru.company.reference.repository;

import ru.company.reference.entity.Room;
import ru.company.reference.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {

    List<Room> findByOfficeId(String officeId);

    List<Room> findByType(RoomType type);

    List<Room> findByCapacityGreaterThanEqual(Integer capacity);

    @Query("SELECT r FROM Room r JOIN r.equipment e WHERE e.id = :equipmentId")
    List<Room> findRoomsWithEquipment(@Param("equipmentId") String equipmentId);

    @Query("SELECT r FROM Room r WHERE r.office.id = :officeId AND r.capacity >= :minCapacity AND r.active = true")
    List<Room> findAvailableRoomsInOffice(@Param("officeId") String officeId,
                                          @Param("minCapacity") Integer minCapacity);

    List<Room> findByOfficeIdAndActiveTrue(String officeId);

    @Query("SELECT r FROM Room r WHERE r.office.id = :officeId AND r.active = true")
    List<Room> findActiveRoomsByOffice(@Param("officeId") String officeId);

    Optional<Object> findByRoomNumber(String roomNumber);
}