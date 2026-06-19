package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.dto.imports.ImportResult;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maps project1 Excel export (xlsx) to v2 {@link OntologyExchangeDocument}.
 *
 * <p>Phase 3b — compiles Sheet A/B/C into {@code spec.project.dataModel.entities}.</p>
 */
@Component
@RequiredArgsConstructor
public class ExcelExchangeMapper {

    private final ExcelOntologyImportAdapter sheetAAdapter;
    private final ExcelBImportAdapter sheetBAdapter;
    private final ExcelCImportAdapter sheetCAdapter;
    private final ObjectMapper objectMapper;

    /**
     * Compile an xlsx stream into a v2 OntologyExchange document.
     */
    public OntologyExchangeDocument mapFromXlsx(InputStream xlsxStream, String externalId) throws IOException {
        byte[] bytes = xlsxStream.readAllBytes();

        String projectId = externalId != null && !externalId.isBlank()
                ? externalId : UUID.randomUUID().toString();
        String projectName = projectId;
        String description = null;

        try (InputStream sheetAStream = new ByteArrayInputStream(bytes)) {
            ImportResult<com.ontology.platform.domain.entity.Ontology> sheetA =
                    sheetAAdapter.execute(sheetAStream, "excel-import");
            if (!sheetA.getImported().isEmpty()) {
                var ontology = sheetA.getImported().get(0);
                projectId = ontology.getName() != null ? ontology.getName() : projectId;
                projectName = ontology.getName() != null ? ontology.getName() : projectName;
                description = ontology.getDescription();
            }
        }

        List<OntologyExchangeDocument.Entity> entities = new ArrayList<>();

        try (InputStream sheetBStream = new ByteArrayInputStream(bytes)) {
            ImportResult<ObjectType> sheetB = sheetBAdapter.execute(sheetBStream, projectId, "excel-import");
            for (ObjectType ot : sheetB.getImported()) {
                entities.add(mapObjectType(ot, "aggregate_root"));
            }
        }

        try (InputStream sheetCStream = new ByteArrayInputStream(bytes)) {
            ImportResult<ObjectType> sheetC = sheetCAdapter.execute(sheetCStream, projectId, "excel-import");
            for (ObjectType ot : sheetC.getImported()) {
                entities.add(mapObjectType(ot, "child_entity"));
            }
        }

        String now = Instant.now().toString();
        return OntologyExchangeDocument.builder()
                .apiVersion("ontology.platform/v2")
                .kind("OntologyExchange")
                .metadata(OntologyExchangeDocument.Metadata.builder()
                        .id(projectId)
                        .version("0.1.0")
                        .name(projectName)
                        .description(description)
                        .source("excel-import")
                        .status("draft")
                        .projectId(projectId)
                        .exportedAt(now)
                        .build())
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(OntologyExchangeDocument.OntologyProject.builder()
                                .id(projectId)
                                .name(projectName)
                                .description(description)
                                .dataModel(OntologyExchangeDocument.DataModel.builder()
                                        .id(projectId + "-data")
                                        .name(projectName + " DataModel")
                                        .version("0.1.0")
                                        .entities(entities)
                                        .build())
                                .createdAt(now)
                                .updatedAt(now)
                                .build())
                        .build())
                .build();
    }

    /**
     * Serialize document to JSON string for the import pipeline.
     */
    public String toJson(OntologyExchangeDocument doc) throws IOException {
        return objectMapper.writeValueAsString(doc);
    }

    /**
     * Parse pre-compiled parsedData JSON (OntologyProject or full OntologyExchange).
     */
    public OntologyExchangeDocument mapFromParsedData(String parsedDataJson) throws IOException {
        var node = objectMapper.readTree(parsedDataJson);
        if (node.has("apiVersion") && node.has("kind")) {
            return objectMapper.treeToValue(node, OntologyExchangeDocument.class);
        }
        // Bare OntologyProject — wrap in envelope
        var project = objectMapper.treeToValue(node, OntologyExchangeDocument.OntologyProject.class);
        String projectId = project.getId() != null ? project.getId() : UUID.randomUUID().toString();
        String now = Instant.now().toString();
        return OntologyExchangeDocument.builder()
                .apiVersion("ontology.platform/v2")
                .kind("OntologyExchange")
                .metadata(OntologyExchangeDocument.Metadata.builder()
                        .id(projectId)
                        .version("0.1.0")
                        .name(project.getName())
                        .source("parsed-data")
                        .status("draft")
                        .projectId(projectId)
                        .exportedAt(now)
                        .build())
                .spec(OntologyExchangeDocument.Spec.builder().project(project).build())
                .build();
    }

    private OntologyExchangeDocument.Entity mapObjectType(ObjectType ot, String entityRole) {
        return OntologyExchangeDocument.Entity.builder()
                .id(ot.getName())
                .name(ot.getDisplayName() != null ? ot.getDisplayName() : ot.getName())
                .description(ot.getDescription())
                .entityRole(entityRole)
                .parentAggregateId("child_entity".equals(entityRole) ? ot.getParentId() : null)
                .build();
    }
}
