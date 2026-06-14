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
@TableName("causality")
public class CausalityPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("ontology_id")
    private String ontologyId;

    @TableField("cause_event_id")
    private String causeEventId;

    @TableField("effect_event_id")
    private String effectEventId;

    @TableField("description")
    private String description;

    @TableField("delay_ms")
    private Integer delayMs;

    @TableField("condition")
    private String condition;

    @TableField("created_at")
    private Instant createdAt;
}
