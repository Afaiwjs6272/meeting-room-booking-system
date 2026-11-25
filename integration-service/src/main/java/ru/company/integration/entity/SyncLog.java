package ru.company.integration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "sync_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String jobId;
    private String externalId;
    private String entityType;
    private String operation;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreatedDate
    private LocalDateTime createdAt;
}