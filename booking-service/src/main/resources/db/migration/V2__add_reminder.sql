CREATE TABLE IF NOT EXISTS booking_participants (
                                                    booking_id VARCHAR(36),
                                                    participant_id VARCHAR(36),
                                                    PRIMARY KEY (booking_id, participant_id),
                                                    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS reminder_sent BOOLEAN DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_bookings_reminder ON bookings(reminder_sent, start_time) WHERE status = 'CONFIRMED';
CREATE INDEX IF NOT EXISTS idx_booking_participants ON booking_participants(participant_id);