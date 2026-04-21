package com.ontology.platform.domain.event;

import com.ontology.platform.common.enums.OntologyStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * 本体归档事件
 * Ontology Archived Event
 */
@Getter
public class OntologyArchivedEvent extends ApplicationEvent {

    private final String ontologyId;
    private final String tenantId;
    private final String name;
    private final String version;
    private final OntologyStatus previousStatus;
    private final OntologyStatus currentStatus;
    private final String archivedBy;
    private final Instant archivedAt;

    public OntologyArchivedEvent(Object source, String ontologyId, String tenantId,
                                  String name, String version,
                                  OntologyStatus previousStatus, OntologyStatus currentStatus,
                                  String archivedBy) {
        super(source);
        this.ontologyId = ontologyId;
        this.tenantId = tenantId;
        this.name = name;
        this.version = version;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
        this.archivedBy = archivedBy;
        this.archivedAt = Instant.now();
    }
}
