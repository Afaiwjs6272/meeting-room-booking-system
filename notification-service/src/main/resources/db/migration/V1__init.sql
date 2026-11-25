DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS user_notification_preferences CASCADE;
DROP TABLE IF EXISTS user_notification_event_preferences CASCADE;

CREATE TABLE IF NOT EXISTS notifications (
                                             id VARCHAR(36) PRIMARY KEY,
                                             user_id VARCHAR(36) NOT NULL,
                                             channel VARCHAR(20) NOT NULL,
                                             recipient VARCHAR(255),
                                             subject VARCHAR(500),
                                             content TEXT,
                                             template_code VARCHAR(100),
                                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                             retry_count INTEGER DEFAULT 0,
                                             error_message TEXT,
                                             related_entity_id VARCHAR(36),
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             sent_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_notification_preferences (
                                                             user_id VARCHAR(36) PRIMARY KEY,
                                                             email_enabled BOOLEAN DEFAULT TRUE,
                                                             email_address VARCHAR(255),
                                                             telegram_enabled BOOLEAN DEFAULT FALSE,
                                                             telegram_chat_id VARCHAR(100),
                                                             webhook_enabled BOOLEAN DEFAULT FALSE,
                                                             webhook_url VARCHAR(500),
                                                             digest_enabled BOOLEAN DEFAULT FALSE,
                                                             digest_time VARCHAR(5),
                                                             quiet_hours_enabled BOOLEAN DEFAULT FALSE,
                                                             quiet_hours_start VARCHAR(5),
                                                             quiet_hours_end VARCHAR(5)
);

CREATE TABLE IF NOT EXISTS user_notification_event_preferences (
                                                                   user_id VARCHAR(36),
                                                                   event_type VARCHAR(50),
                                                                   enabled BOOLEAN DEFAULT TRUE,
                                                                   PRIMARY KEY (user_id, event_type),
                                                                   FOREIGN KEY (user_id) REFERENCES user_notification_preferences(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);