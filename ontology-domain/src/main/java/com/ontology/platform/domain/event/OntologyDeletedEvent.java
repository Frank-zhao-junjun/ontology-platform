package com.ontology.platform.domain.event;

import com.ontology.platform.common.enums.OntologyStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * 本体删除事件
 * Ontology Deleted Event
 */
@Getter
public class OntologyDeletedEvent extends ApplicationEvent {

    private final String ontologyId;
    private final String tenantId;
    private final String name;
    private final String deletedBy;
    private final Instant deletedAt;

    public OntologyDeletedEvent(Object source, String ontologyId, String tenantId,
                                String name, String deletedBy) {
        super(source);
        this.ontologyId = ontologyId;
        this.tenantId = tenantId;
        this.name = name;
        this.deletedBy = deletedBy;
        this.deletedAt = Instant.now();
    }
}
