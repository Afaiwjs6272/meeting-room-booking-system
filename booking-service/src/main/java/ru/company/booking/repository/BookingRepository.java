package ru.company.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.company.booking.entity.Booking;
import ru.company.booking.entity.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    Page<Booking> findByUserIdAndStatusNot(String userId, BookingStatus status, Pageable pageable);

    List<Booking> findByRoomIdAndStartTimeBetween(String roomId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.roomId = :roomId " +
            "AND b.status != 'CANCELLED' " +
            "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findOverlappingBookings(@Param("roomId") String roomId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    List<Booking> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' " +
            "AND b.reminderSent = false " +
            "AND b.startTime BETWEEN :start AND :end")
    List<Booking> findBookingsNeedingReminder(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' " +
            "AND b.reminderSent = false " +
            "AND b.startTime <= :time")
    List<Booking> findBookingsWithStartTimeBefore(@Param("time") LocalDateTime time);

    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' " +
            "AND b.endTime < :currentTime")
    List<Booking> findExpiredBookings(@Param("currentTime") LocalDateTime currentTime);

    List<Booking> findByStartTimeAfter(LocalDateTime time);
}