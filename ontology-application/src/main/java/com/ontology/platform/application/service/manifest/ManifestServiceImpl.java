package com.ontology.platform.application.service.manifest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.dto.manifest.*;
import com.ontology.platform.domain.vo.manifest.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ManifestServiceImpl implements ManifestService {

    private final ManifestValidator validator = new ManifestValidator();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // In-memory store (Phase 2: replace with MyBatis-Plus)
    private final Map<String, ManifestImportRecord> importStore = new ConcurrentHashMap<>();
    private final Map<String, ManifestDocument> versionStore = new ConcurrentHashMap<>();

    @Override
    public ImportManifestResponse importManifest(ImportManifestRequest request) {
        ManifestDocument doc;
        try {
            doc = objectMapper.readValue(request.getRawContent(), ManifestDocument.class);
        } catch (Exception e) {
            log.error("Failed to parse manifest: {}", e.getMessage());
            return ImportManifestResponse.builder()
                    .valid(false)
                    .errors(List.of(ValidationError.of("PARSE", "ROOT", null, null, "Parse error: " + e.getMessage())))
                    .build();
        }

        ManifestValidationResult result = validator.validate(doc);
        if (!result.isValid()) {
            return ImportManifestResponse.builder()
                    .valid(false).errors(result.getErrors()).warnings(result.getWarnings()).build();
        }

        String importId = UUID.randomUUID().toString();
        String externalId = doc.getMetadata() != null ? doc.getMetadata().getId() : importId;

        importStore.put(importId, new ManifestImportRecord(importId, externalId, doc, "DRAFT", request.getCreatedBy()));

        Map<String, Integer> counts = new HashMap<>();
        counts.put("objectTypes", doc.getObjectTypes().size());
        counts.put("actions", doc.getActions().size());
        counts.put("events", doc.getEvents().size());
        counts.put("rules", doc.getRules().size());
        counts.put("stateMachines", doc.getStateMachines().size());

        log.info("Manifest imported: draftId={}, externalId={}, counts={}", importId, externalId, counts);

        return ImportManifestResponse.builder()
                .valid(true).draftId(importId).externalId(externalId)
                .importedCounts(counts).warnings(result.getWarnings()).build();
    }

    @Override
    public ManifestPreviewResponse preview(String importId) {
        ManifestImportRecord record = importStore.get(importId);
        if (record == null) throw new RuntimeException("Import not found: " + importId);

        // Phase 2: compare with previous published version
        return ManifestPreviewResponse.builder()
                .importId(importId)
                .changes(List.of(new ManifestPreviewResponse.ChangeItem("meta", importId, "new import")))
                .build();
    }

    @Override
    public ManifestPublishResponse publish(String importId) {
        ManifestImportRecord record = importStore.get(importId);
        if (record == null) throw new RuntimeException("Import not found: " + importId);
        record.status = "PUBLISHED";
        record.publishedAt = Instant.now();

        String version = record.doc.getMetadata() != null ? record.doc.getMetadata().getVersion() : "0.1.0";
        versionStore.put(version, record.doc);

        log.info("Manifest published: id={}, version={}", importId, version);
        return ManifestPublishResponse.builder().version(version).publishedAt(record.publishedAt).build();
    }

    @Override
    public ManifestDocument export(String versionId, String format) {
        ManifestDocument doc = versionStore.get(versionId);
        if (doc == null) throw new RuntimeException("Version not found: " + versionId);
        return doc;
    }

    // Internal record class
    static class ManifestImportRecord {
        String id; String externalId; ManifestDocument doc;
        String status; String createdBy; Instant createdAt;
        Instant publishedAt;
        ManifestImportRecord(String id, String externalId, ManifestDocument doc, String status, String createdBy) {
            this.id = id; this.externalId = externalId; this.doc = doc;
            this.status = status; this.createdBy = createdBy; this.createdAt = Instant.now();
        }
    }
}
