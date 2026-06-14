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
@TableName("state_machine")
public class StateMachinePO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;

    @TableField("entity_id")
    private String entityId;

    @TableField("name")
    private String name;

    @TableField("initial_state")
    private String initialState;

    @TableField("states")
    private String states;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableField("deleted")
    private Boolean deleted;
}
