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
@TableName("agent_intent")
public class AgentIntentPO {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("trigger_phrases")
    private String triggerPhrases;

    @TableField("action_id")
    private String actionId;

    @TableField("category")
    private String category;

    @TableField("target_entity_id")
    private String targetEntityId;

    @TableField("priority")
    private Integer priority;

    @TableField("requires_confirmation")
    private Boolean requiresConfirmation;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
