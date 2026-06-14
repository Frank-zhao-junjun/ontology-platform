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
@TableName("state_transition")
public class StateTransitionPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("state_machine_id")
    private String stateMachineId;

    @TableField("from_state")
    private String fromState;

    @TableField("to_state")
    private String toState;

    @TableField("trigger")
    private String triggerName;

    @TableField("guard_condition")
    private String guardCondition;

    @TableField("created_at")
    private Instant createdAt;
}
