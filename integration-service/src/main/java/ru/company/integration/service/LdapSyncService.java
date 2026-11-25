package ru.company.integration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.company.integration.client.ReferenceServiceClient;
import ru.company.integration.entity.SyncJob;
import ru.company.integration.repository.SyncJobRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LdapSyncService {

    private final ReferenceServiceClient referenceServiceClient;
    private final SyncJobRepository syncJobRepository;

    @Value("${integration.ldap.enabled:false}")
    private boolean ldapEnabled;

    @Transactional
    public void syncUsers() {
        if (!ldapEnabled) {
            log.info("LDAP integration is disabled");
            return;
        }

        log.info("Starting LDAP user sync");
        SyncJob job = SyncJob.builder()
                .id(UUID.randomUUID().toString())
                .jobType("LDAP_SYNC")
                .status("RUNNING")
                .startTime(LocalDateTime.now())
                .build();

        syncJobRepository.save(job);

        try {
            // Simulate LDAP fetch
            log.info("Fetching users from LDAP...");

            job.setStatus("COMPLETED");
            job.setProcessedRecords(5);
            job.setSuccessRecords(5);

        } catch (Exception e) {
            log.error("LDAP sync failed: {}", e.getMessage());
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
        } finally {
            job.setEndTime(LocalDateTime.now());
            syncJobRepository.save(job);
        }
    }
}