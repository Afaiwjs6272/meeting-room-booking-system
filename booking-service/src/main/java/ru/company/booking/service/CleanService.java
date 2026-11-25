package ru.company.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.company.booking.entity.Booking;
import ru.company.booking.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanService {

    private final BookingRepository bookingRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void deleteExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();

        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(now);

        if (!expiredBookings.isEmpty()) {
            log.info("Found {} expired bookings to delete", expiredBookings.size());

            for (Booking booking : expiredBookings) {
                bookingRepository.delete(booking);
                log.info("Deleted expired booking: id={}, roomId={}, endTime={}",
                        booking.getId(), booking.getRoomId(), booking.getEndTime());
            }
        }
    }
}