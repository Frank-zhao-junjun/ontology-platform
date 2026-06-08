package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "business_scenarios", uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "code"}))
@Getter
@Setter
public class BusinessScenarioEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = false, length = 80)
    private String code;
    @Column(name = "name_en", length = 200)
    private String nameEn;
    private String description;
    @Column(name = "applicable_object_type_ids_json", nullable = false, columnDefinition = "TEXT")
    private String applicableObjectTypeIdsJson = "[]";
    @Column(name = "created_at")
    private Instant createdAt;
}
