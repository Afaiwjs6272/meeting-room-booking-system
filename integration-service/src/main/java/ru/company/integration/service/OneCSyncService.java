package ru.company.integration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.company.integration.client.ReferenceServiceClient;
import ru.company.integration.entity.SyncJob;
import ru.company.integration.repository.SyncLogRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OneCSyncService {

    private final ReferenceServiceClient referenceServiceClient;
    private final SyncLogRepository syncJobRepository;
    private final SyncLogRepository syncLogRepository;

    @Value("${integration.onc.enabled:false}")
    private boolean oneCEnabled;

    @Transactional
    public void syncEmployees() {
        if (!oneCEnabled) {
            log.info("1C integration is disabled");
            return;
        }

        log.info("Starting 1C employee sync");
        SyncJob job = SyncJob.builder()
                .id(UUID.randomUUID().toString())
                .jobType("ONEC_SYNC")
                .status("RUNNING")
                .startTime(LocalDateTime.now())
                .build();

        syncJobRepository.save(job);

        try {
            // Simulate fetching from 1C
            // In real implementation, call 1C API here
            log.info("Fetching employees from 1C...");

            // Process employees
            // For each employee, check if exists, create or update

            job.setStatus("COMPLETED");
            job.setProcessedRecords(10);
            job.setSuccessRecords(10);

        } catch (Exception e) {
            log.error("1C sync failed: {}", e.getMessage());
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
        } finally {
            job.setEndTime(LocalDateTime.now());
            syncJobRepository.save(job);
        }
    }
}