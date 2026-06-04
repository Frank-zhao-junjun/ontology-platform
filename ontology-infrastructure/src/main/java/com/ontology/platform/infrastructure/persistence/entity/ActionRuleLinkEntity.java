package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "action_rule_links")
@IdClass(ActionRuleLinkEntity.Key.class)
@Getter
@Setter
public class ActionRuleLinkEntity {
    @Id
    @Column(name = "action_id", length = 36)
    private String actionId;
    @Id
    @Column(name = "rule_id", length = 36)
    private String ruleId;
    @Column(name = "sort_order")
    private int sortOrder;

    @Getter
    @Setter
    public static class Key implements Serializable {
        private String actionId;
        private String ruleId;
    }
}
