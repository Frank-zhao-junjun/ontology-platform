package com.ontology.platform.domain.event;

import com.ontology.platform.common.enums.OntologyStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * 本体发布事件
 * Ontology Published Event
 */
@Getter
public class OntologyPublishedEvent extends ApplicationEvent {

    private final String ontologyId;
    private final String tenantId;
    private final String name;
    private final String version;
    private final OntologyStatus previousStatus;
    private final OntologyStatus currentStatus;
    private final String publishedBy;
    private final Instant publishedAt;

    public OntologyPublishedEvent(Object source, String ontologyId, String tenantId,
                                    String name, String version,
                                    OntologyStatus previousStatus, OntologyStatus currentStatus,
                                    String publishedBy) {
        super(source);
        this.ontologyId = ontologyId;
        this.tenantId = tenantId;
        this.name = name;
        this.version = version;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
        this.publishedBy = publishedBy;
        this.publishedAt = Instant.now();
    }
}
