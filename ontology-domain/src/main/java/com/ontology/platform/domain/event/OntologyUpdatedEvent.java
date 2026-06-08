package com.ontology.platform.domain.event;

import com.ontology.platform.common.enums.OntologyStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * 本体更新事件
 * Ontology Updated Event
 */
@Getter
public class OntologyUpdatedEvent extends ApplicationEvent {

    private final String ontologyId;
    private final String tenantId;
    private final String name;
    private final String displayName;
    private final String description;
    private final String updatedBy;
    private final Instant updatedAt;

    public OntologyUpdatedEvent(Object source, String ontologyId, String tenantId,
                                 String name, String displayName, String description, String updatedBy) {
        super(source);
        this.ontologyId = ontologyId;
        this.tenantId = tenantId;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }
}
