package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "metrics", uniqueConstraints = @UniqueConstraint(columnNames = {"context_id", "manifest_code"}))
@Getter
@Setter
public class MetricEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", nullable = false, length = 36)
    private String contextId;
    @Column(name = "manifest_code", nullable = false, length = 80)
    private String manifestCode;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(name = "name_en", length = 200)
    private String nameEn;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String formula;
    @Column(name = "data_source_ref_json", nullable = false, columnDefinition = "TEXT")
    private String dataSourceRefJson = "[]";
    @Column(name = "aggregation_dimensions_json", columnDefinition = "TEXT")
    private String aggregationDimensionsJson = "[]";
    @Column(length = 100)
    private String period;
    @Column(name = "created_at")
    private Instant createdAt;
}
