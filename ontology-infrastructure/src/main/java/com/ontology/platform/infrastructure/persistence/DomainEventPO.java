package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("domain_event")
public class DomainEventPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;

    @TableField("entity_id")
    private String entityId;

    @TableField("name")
    private String name;

    @TableField("display_name")
    private String displayName;

    @TableField("description")
    private String description;

    @TableField("event_type")
    private String eventType;

    @TableField("severity")
    private String severity;

    @TableField("payload_schema")
    private String payloadSchema;

    @TableField("source")
    private String source;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableField("deleted")
    private Boolean deleted;
}
