package ru.company.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationEvent extends BaseEvent {
    private List<String> recipientIds;
    private String templateCode;
    private Map<String, Object> templateVariables;
    private List<String> channels;
    private Integer priority;
    private Boolean sendDigest;
}