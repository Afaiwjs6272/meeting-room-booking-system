package ru.company.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.company.integration.entity.SyncJob;

import java.util.List;

@Repository
public interface SyncJobRepository extends JpaRepository<SyncJob, String> {

    List<SyncJob> findByStatus(String status);
}