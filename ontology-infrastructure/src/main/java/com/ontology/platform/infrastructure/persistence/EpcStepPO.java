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
@TableName("epc_step")
public class EpcStepPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;

    @TableField("flow_name")
    private String flowName;

    @TableField("step_order")
    private Integer stepOrder;

    @TableField("trigger_event_id")
    private String triggerEventId;

    @TableField("action_id")
    private String actionId;

    @TableField("conditions")
    private String conditions;

    @TableField("guards")
    private String guards;

    @TableField("timeout_ms")
    private Integer timeoutMs;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
