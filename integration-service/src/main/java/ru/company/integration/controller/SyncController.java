package ru.company.integration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.company.common.response.ApiResponse;
import ru.company.integration.entity.SyncJob;
import ru.company.integration.repository.SyncJobRepository;
import ru.company.integration.service.LdapSyncService;
import ru.company.integration.service.OneCSyncService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Tag(name = "Integration Sync", description = "APIs for triggering sync jobs")
public class SyncController {

    private final OneCSyncService oneCSyncService;
    private final LdapSyncService ldapSyncService;
    private final SyncJobRepository syncJobRepository;

    @PostMapping("/onec/employees")
    @Operation(summary = "Trigger 1C employee sync")
    public ResponseEntity<ApiResponse<String>> syncOneC() {
        oneCSyncService.syncEmployees();
        return ResponseEntity.ok(ApiResponse.success("1C sync triggered"));
    }

    @PostMapping("/ldap/users")
    @Operation(summary = "Trigger LDAP user sync")
    public ResponseEntity<ApiResponse<String>> syncLdap() {
        ldapSyncService.syncUsers();
        return ResponseEntity.ok(ApiResponse.success("LDAP sync triggered"));
    }

    @GetMapping("/jobs")
    @Operation(summary = "Get all sync jobs")
    public ResponseEntity<ApiResponse<List<SyncJob>>> getSyncJobs() {
        return ResponseEntity.ok(ApiResponse.success(syncJobRepository.findAll()));
    }

    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "Get sync job status")
    public ResponseEntity<ApiResponse<SyncJob>> getSyncJob(@PathVariable String jobId) {
        return ResponseEntity.ok(ApiResponse.success(
                syncJobRepository.findById(jobId).orElse(null)));
    }
}