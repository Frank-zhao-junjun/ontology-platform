package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.ReportDefinition;
import com.ontology.platform.infrastructure.persistence.ReportDefinitionPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportDefinitionConverter {

    public ReportDefinition toEntity(ReportDefinitionPO po) {
        if (po == null) return null;
        return ReportDefinition.builder()
                .id(po.getId())
                .ontologyId(po.getOntologyId())
                .reportName(po.getReportName())
                .description(po.getDescription())
                .reportFormat(po.getReportFormat())
                .fields(po.getFields())
                .dataSource(po.getDataSource())
                .queryId(po.getQueryId())
                .scheduleCron(po.getScheduleCron())
                .enabled(po.getEnabled())
                .config(po.getConfig())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .deleted(po.getDeleted())
                .build();
    }

    public ReportDefinitionPO toPO(ReportDefinition entity) {
        if (entity == null) return null;
        return ReportDefinitionPO.builder()
                .id(entity.getId())
                .ontologyId(entity.getOntologyId())
                .reportName(entity.getReportName())
                .description(entity.getDescription())
                .reportFormat(entity.getReportFormat())
                .fields(entity.getFields())
                .dataSource(entity.getDataSource())
                .queryId(entity.getQueryId())
                .scheduleCron(entity.getScheduleCron())
                .enabled(entity.getEnabled())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    public List<ReportDefinition> toEntityList(List<ReportDefinitionPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
