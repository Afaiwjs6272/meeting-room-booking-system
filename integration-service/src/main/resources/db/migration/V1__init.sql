CREATE TABLE IF NOT EXISTS sync_jobs (
                                         id VARCHAR(36) PRIMARY KEY,
                                         job_type VARCHAR(50) NOT NULL,
                                         status VARCHAR(20) NOT NULL,
                                         start_time TIMESTAMP,
                                         end_time TIMESTAMP,
                                         total_records INTEGER DEFAULT 0,
                                         processed_records INTEGER DEFAULT 0,
                                         success_records INTEGER DEFAULT 0,
                                         failed_records INTEGER DEFAULT 0,
                                         error_message TEXT,
                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sync_logs (
                                         id VARCHAR(36) PRIMARY KEY,
                                         job_id VARCHAR(36) NOT NULL,
                                         external_id VARCHAR(255) NOT NULL,
                                         entity_type VARCHAR(50) NOT NULL,
                                         operation VARCHAR(20) NOT NULL,
                                         status VARCHAR(20),
                                         error_message TEXT,
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sync_jobs_status ON sync_jobs(status);
CREATE INDEX idx_sync_logs_job_id ON sync_logs(job_id);