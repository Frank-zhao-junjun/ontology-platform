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
@TableName("notification_definition")
public class NotificationDefinitionPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;
    @TableField("notif_name")
    private String notifName;
    @TableField("description")
    private String description;
    @TableField("channel")
    private String channel;
    @TableField("template")
    private String template;
    @TableField("recipients")
    private String recipients;
    @TableField("trigger_event")
    private String triggerEvent;
    @TableField("enabled")
    private Boolean enabled;
    @TableField("config")
    private String config;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableField("deleted")
    private Boolean deleted;
}
