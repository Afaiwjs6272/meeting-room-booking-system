package ru.company.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.company.common.event.NotificationEvent;
import ru.company.common.reference.EmployeeResponse;
import ru.company.common.response.ApiResponse;
import ru.company.notification.client.ReferenceServiceClient;
import ru.company.notification.entity.Notification;
import ru.company.notification.entity.NotificationStatus;
import ru.company.notification.entity.UserNotificationPreference;
import ru.company.notification.repository.NotificationRepository;
import ru.company.notification.repository.UserNotificationPreferenceRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final ReferenceServiceClient referenceServiceClient;
    private final NotificationRepository notificationRepository;
    private final UserNotificationPreferenceRepository preferenceRepository;
    @Transactional
    public void processNotification(NotificationEvent event) {
        log.info("Processing notification for user: {}", event.getUserId());

        try {
            UserNotificationPreference preferences = preferenceRepository.findById(event.getUserId())
                    .orElseGet(() -> createDefaultPreferences(event.getUserId()));

            ApiResponse<EmployeeResponse> apiResponse = referenceServiceClient.getEmployee(event.getUserId());

            if (apiResponse == null || !apiResponse.isSuccess() || apiResponse.getData() == null) {
                log.warn("User not found or API error: {}", event.getUserId());
                return;
            }

            EmployeeResponse user = apiResponse.getData();

            String subject = "Booking Confirmation";
            String emailContent = buildEmailContent(event, user);

            // Send via Email
            if (preferences.getEmailEnabled() && user.getEmail() != null && !user.getEmail().isEmpty()) {
                emailService.sendEmail(user.getEmail(), subject, emailContent);
                saveNotification(event.getUserId(), "EMAIL", user.getEmail(), subject, emailContent, NotificationStatus.SENT);
                log.info("Email sent to: {}", user.getEmail());
            }

        } catch (Exception e) {
            log.error("Failed to process notification: {}", e.getMessage(), e);
            saveNotification(event.getUserId(), "UNKNOWN", null, null, null, NotificationStatus.FAILED);
        }
    }


    private String buildEmailContent(NotificationEvent event, EmployeeResponse user) {
        Map<String, Object> vars = event.getTemplateVariables();
        if (vars == null) {
            vars = Map.of();
        }

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String userName = (firstName + " " + lastName).trim();
        if (userName.isEmpty()) {
            userName = "User";
        }

        // Get values safely
        String title = safeGetString(vars, "title", "Meeting");
        String roomName = safeGetString(vars, "roomName", "Conference Room");
        String roomNumber = safeGetString(vars, "roomNumber", "");
        Object startTimeObj = vars.get("startTime");
        Object endTimeObj = vars.get("endTime");

        // Format room info
        String roomInfo = roomName;
        if (!roomNumber.isEmpty()) {
            roomInfo = roomName + " (" + roomNumber + ")";
        }

        // Format time properly
        String formattedStartTime = formatTimeValue(startTimeObj);
        String formattedEndTime = formatTimeValue(endTimeObj);

        return String.format(
                "Dear %s,\n\n" +
                        "Your booking has been confirmed:\n\n" +
                        "Meeting: %s\n" +
                        "Room: %s\n" +
                        "Time: %s - %s\n\n" +
                        "Thank you for using our service!",
                userName, title, roomInfo, formattedStartTime, formattedEndTime
        );
    }

    private String formatTimeValue(Object timeObj) {
        if (timeObj == null) {
            return "Unknown time";
        }

        try {
            if (timeObj instanceof List) {
                // Handle array format like [2028, 10, 1, 10, 0]
                List<?> list = (List<?>) timeObj;
                if (list.size() >= 5) {
                    int year = ((Number) list.get(0)).intValue();
                    int month = ((Number) list.get(1)).intValue();
                    int day = ((Number) list.get(2)).intValue();
                    int hour = ((Number) list.get(3)).intValue();
                    int minute = ((Number) list.get(4)).intValue();
                    return String.format("%02d.%02d.%04d %02d:%02d", day, month, year, hour, minute);
                }
            } else if (timeObj instanceof String) {
                String timeStr = (String) timeObj;
                // Try to parse ISO format
                if (timeStr.contains("T")) {
                    LocalDateTime dt = LocalDateTime.parse(timeStr);
                    return dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                }
                return timeStr;
            } else if (timeObj instanceof LocalDateTime) {
                return ((LocalDateTime) timeObj).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            }
            return timeObj.toString();
        } catch (Exception e) {
            log.warn("Failed to format time: {}", timeObj, e);
            return timeObj.toString();
        }
    }
    private String safeGetString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    private void saveNotification(String userId, String channel, String recipient,
                                  String subject, String content, NotificationStatus status) {
        log.info("=== SAVING NOTIFICATION TO DB ===");
        log.info("  userId: {}", userId);
        log.info("  channel: {}", channel);
        log.info("  recipient: {}", recipient);
        log.info("  status: {}", status);
        Notification notification = Notification.builder()
                .userId(userId)
                .channel(channel)
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .status(status)
                .retryCount(0)
                .sentAt(status == NotificationStatus.SENT ? LocalDateTime.now() : null)
                .build();

        notificationRepository.save(notification);
    }

    private UserNotificationPreference createDefaultPreferences(String userId) {
        UserNotificationPreference preferences = UserNotificationPreference.builder()
                .userId(userId)
                .emailEnabled(true)
                .telegramEnabled(false)
                .webhookEnabled(false)
                .digestEnabled(false)
                .quietHoursEnabled(false)
                .build();

        return preferenceRepository.save(preferences);
    }
}