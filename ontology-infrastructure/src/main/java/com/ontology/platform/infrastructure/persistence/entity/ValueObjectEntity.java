package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "value_objects", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@Setter
public class ValueObjectEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = false, length = 80)
    private String code;
    @Column(name = "name_en", length = 200)
    private String nameEn;
    private String description;
    @Column(name = "properties_json", nullable = false, columnDefinition = "TEXT")
    private String propertiesJson = "[]";
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
