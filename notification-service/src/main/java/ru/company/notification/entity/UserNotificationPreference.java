package ru.company.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Entity
@Table(name = "user_notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationPreference {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "telegram_enabled")
    private Boolean telegramEnabled = false;

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    @Column(name = "webhook_enabled")
    private Boolean webhookEnabled = false;

    @Column(name = "webhook_url")
    private String webhookUrl;

    @ElementCollection
    @CollectionTable(name = "user_notification_event_preferences",
            joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "event_type")
    @Column(name = "enabled")
    private Map<String, Boolean> eventPreferences;

    @Column(name = "digest_enabled")
    private Boolean digestEnabled = false;

    @Column(name = "digest_time")
    private String digestTime;

    @Column(name = "quiet_hours_enabled")
    private Boolean quietHoursEnabled = false;

    @Column(name = "quiet_hours_start")
    private String quietHoursStart;

    @Column(name = "quiet_hours_end")
    private String quietHoursEnd;
}