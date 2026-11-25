-- Удалите старую таблицу если она существует
DROP TABLE IF EXISTS bookings CASCADE;

-- Создайте таблицу заново
CREATE TABLE IF NOT EXISTS bookings (
                                        id VARCHAR(36) PRIMARY KEY,
                                        room_id VARCHAR(36) NOT NULL,
                                        user_id VARCHAR(36) NOT NULL,
                                        title VARCHAR(255) NOT NULL,
                                        description TEXT,
                                        start_time TIMESTAMP NOT NULL,
                                        end_time TIMESTAMP NOT NULL,
                                        status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        cancelled_at TIMESTAMP,
                                        cancelled_by VARCHAR(36)
);

-- Создайте индексы
CREATE INDEX idx_bookings_room_time ON bookings(room_id, start_time, end_time);
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_status ON bookings(status);