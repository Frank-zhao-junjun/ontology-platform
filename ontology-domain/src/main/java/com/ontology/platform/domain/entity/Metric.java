package com.ontology.platform.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class Metric {
    private final String id, contextId, manifestCode, name, nameEn, formula;
    private final String dataSourceRefJson, aggregationDimensionsJson;
    private final String period;
    private final Instant createdAt;

    @Builder
    public Metric(String id, String contextId, String manifestCode, String name, String nameEn,
                  String formula, String dataSourceRefJson, String aggregationDimensionsJson,
                  String period, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.manifestCode = manifestCode;
        this.name = name;
        this.nameEn = nameEn;
        this.formula = formula;
        this.dataSourceRefJson = dataSourceRefJson != null ? dataSourceRefJson : "[]";
        this.aggregationDimensionsJson = aggregationDimensionsJson != null ? aggregationDimensionsJson : "[]";
        this.period = period;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static Metric create(String contextId, String manifestCode, String name, String nameEn,
                                String formula, String dataSourceRefJson, String aggregationDimensionsJson,
                                String period) {
        return Metric.builder().contextId(contextId).manifestCode(manifestCode).name(name)
                .nameEn(nameEn).formula(formula).dataSourceRefJson(dataSourceRefJson)
                .aggregationDimensionsJson(aggregationDimensionsJson).period(period).build();
    }
}
