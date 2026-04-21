package com.ontology.platform.domain.event;

import com.ontology.platform.common.enums.OntologyStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * 本体创建事件
 * Ontology Created Event
 */
@Getter
public class OntologyCreatedEvent extends ApplicationEvent {

    private final String ontologyId;
    private final String tenantId;
    private final String name;
    private final String displayName;
    private final String createdBy;
    private final Instant createdAt;

    public OntologyCreatedEvent(Object source, String ontologyId, String tenantId, 
                                  String name, String displayName, String createdBy) {
        super(source);
        this.ontologyId = ontologyId;
        this.tenantId = tenantId;
        this.name = name;
        this.displayName = displayName;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }
}
