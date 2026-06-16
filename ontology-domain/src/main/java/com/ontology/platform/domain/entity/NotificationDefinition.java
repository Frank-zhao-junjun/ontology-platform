package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDefinition {
    private String id;
    private String ontologyId;
    private String notifName;
    private String description;
    private String channel;
    private String template;
    private String recipients;
    private String triggerEvent;
    private Boolean enabled;
    private String config;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;

    public static NotificationDefinition create(String ontologyId, String notifName, String channel) {
        return NotificationDefinition.builder()
                .id(UUID.randomUUID().toString())
                .ontologyId(ontologyId)
                .notifName(notifName)
                .channel(channel)
                .recipients("[]")
                .enabled(true)
                .config("{}")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();
    }
}
