package ru.company.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SyncEvent extends BaseEvent {
    private String jobId;
    private String sourceSystem;
    private String entityType;
    private Integer batchSize;
    private Map<String, Object> parameters;
    private SyncAction action;

    public enum SyncAction {
        FULL, INCREMENTAL, RETRY
    }
}