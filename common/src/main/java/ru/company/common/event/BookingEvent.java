package ru.company.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookingEvent extends BaseEvent {
    private String bookingId;
    private String roomId;
    private String officeId;
    private List<String> participantIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String title;
    private BookingAction action;

    public enum BookingAction {
        CREATE, UPDATE, CANCEL, CONFIRM, REJECT
    }
}