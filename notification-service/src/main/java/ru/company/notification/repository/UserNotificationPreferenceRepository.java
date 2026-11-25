package ru.company.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.company.notification.entity.UserNotificationPreference;

import java.util.Optional;

@Repository
public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, String> {

    Optional<UserNotificationPreference> findByUserId(String userId);
}