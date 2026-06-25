package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.ImportResult;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.entity.ObjectType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Maps project1 Markdown export to v2 {@link OntologyExchangeDocument}.
 * <p>
 * Supports the same format as {@code src/lib/markdown/markdown-import.ts} in project1:
 * <pre>
 * # 本体模型导出
 * > 导出时间: ...
 * ## A-价值域
 * | ID | 名称 | 描述 |
 * |----|------|------|
 * | VD001 | 合同生命周期 | ... |
 *
 * ## B-能力
 * | ID | 名称 | 描述 | 父ID |
 * ...
 * </pre>
 * Sections: A, B, C, EPC, E1~E8.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarkdownExchangeMapper {

    private final ObjectMapper objectMapper;

    /**
     * Section kind constants matching project1's ModuleKind.
     */
    public static final String KIND_A = "A";
    public static final String KIND_B = "B";
    public static final String KIND_C = "C";
    public static final String KIND_EPC = "EPC";
    public static final String KIND_E1 = "E1";
    public static final String KIND_E2 = "E2";
    public static final String KIND_E3 = "E3";
    public static final String KIND_E4 = "E4";
    public static final String KIND_E5 = "E5";
    public static final String KIND_E6 = "E6";
    public static final String KIND_E7 = "E7";
    public static final String KIND_E8 = "E8";

    private static final Map<String, String> SECTION_PREFIX_MAP = new LinkedHashMap<>();

    static {
        SECTION_PREFIX_MAP.put("a-价值域", KIND_A);
        SECTION_PREFIX_MAP.put("a-价值域(value-domain)", KIND_A);
        SECTION_PREFIX_MAP.put("a", KIND_A);
        SECTION_PREFIX_MAP.put("b-能力", KIND_B);
        SECTION_PREFIX_MAP.put("b-能力(capability)", KIND_B);
        SECTION_PREFIX_MAP.put("b", KIND_B);
        SECTION_PREFIX_MAP.put("c-场景", KIND_C);
        SECTION_PREFIX_MAP.put("c-场景(scenario)", KIND_C);
        SECTION_PREFIX_MAP.put("c", KIND_C);
        SECTION_PREFIX_MAP.put("epc流程", KIND_EPC);
        SECTION_PREFIX_MAP.put("epc-process", KIND_EPC);
        SECTION_PREFIX_MAP.put("epc", KIND_EPC);
        SECTION_PREFIX_MAP.put("e1-数据", KIND_E1);
        SECTION_PREFIX_MAP.put("e1-数据(data)", KIND_E1);
        SECTION_PREFIX_MAP.put("e1", KIND_E1);
        SECTION_PREFIX_MAP.put("e2-行为", KIND_E2);
        SECTION_PREFIX_MAP.put("e2-行为(behavior)", KIND_E2);
        SECTION_PREFIX_MAP.put("e2", KIND_E2);
        SECTION_PREFIX_MAP.put("e3-规则", KIND_E3);
        SECTION_PREFIX_MAP.put("e3-规则(rule)", KIND_E3);
        SECTION_PREFIX_MAP.put("e3", KIND_E3);
        SECTION_PREFIX_MAP.put("e4-事件", KIND_E4);
        SECTION_PREFIX_MAP.put("e4-事件(event)", KIND_E4);
        SECTION_PREFIX_MAP.put("e4", KIND_E4);
        SECTION_PREFIX_MAP.put("e5-岗位角色", KIND_E5);
        SECTION_PREFIX_MAP.put("e5-岗位角色(organization)", KIND_E5);
        SECTION_PREFIX_MAP.put("e5", KIND_E5);
        SECTION_PREFIX_MAP.put("e6-指标", KIND_E6);
        SECTION_PREFIX_MAP.put("e6-指标(metric)", KIND_E6);
        SECTION_PREFIX_MAP.put("e6", KIND_E6);
        SECTION_PREFIX_MAP.put("e7-边界约束", KIND_E7);
        SECTION_PREFIX_MAP.put("e7-边界约束(boundary)", KIND_E7);
        SECTION_PREFIX_MAP.put("e7", KIND_E7);
        SECTION_PREFIX_MAP.put("e8-数据源", KIND_E8);
        SECTION_PREFIX_MAP.put("e8-数据源(datasource)", KIND_E8);
        SECTION_PREFIX_MAP.put("e8", KIND_E8);
    }

    private static final Pattern SECTION_PATTERN = Pattern.compile("^##\\s+(.+)$", Pattern.MULTILINE);

    /**
     * Parse result from raw markdown text.
     */
    public record MarkdownParseResult(
            List<SectionData> sections,
            String projectName,
            String exportedAt
    ) {}

    /**
     * A single markdown section (e.g. A-价值域, E1-数据) with its table rows.
     */
    public record SectionData(
            String kind,
            String title,
            List<String> headers,
            List<Map<String, String>> rows
    ) {}

    /**
     * Compile a Markdown document into a v2 OntologyExchangeDocument.
     */
    public OntologyExchangeDocument mapFromMarkdown(String markdownContent, String externalId) throws IOException {
        MarkdownParseResult parsed = parseMarkdown(markdownContent);
        String projectId = (externalId != null && !externalId.isBlank())
                ? externalId : UUID.randomUUID().toString();

        log.info("Markdown import: {} sections, project={}", parsed.sections().size(), projectId);

        // Extract domain from A-价值域
        OntologyExchangeDocument.Domain domain = null;
        for (SectionData sec : parsed.sections()) {
            if (KIND_A.equals(sec.kind()) && !sec.rows().isEmpty()) {
                Map<String, String> first = sec.rows().get(0);
                domain = OntologyExchangeDocument.Domain.builder()
                        .id(first.getOrDefault("id", projectId))
                        .name(first.getOrDefault("name", parsed.projectName()))
                        .description(first.getOrDefault("description", ""))
                        .build();
                break;
            }
        }
        if (domain == null) {
            domain = OntologyExchangeDocument.Domain.builder()
                    .id(projectId)
                    .name(parsed.projectName())
                    .build();
        }

        // Build DataModel from E1
        List<OntologyExchangeDocument.Entity> entities = new ArrayList<>();
        for (SectionData sec : parsed.sections()) {
            if (KIND_E1.equals(sec.kind())) {
                for (Map<String, String> row : sec.rows()) {
                    entities.add(OntologyExchangeDocument.Entity.builder()
                            .id(row.getOrDefault("id", UUID.randomUUID().toString()))
                            .name(row.getOrDefault("name", ""))
                            .description(row.getOrDefault("description", ""))
                            .entityRole("aggregate_root")
                            .attributes(new ArrayList<>())
                            .relations(new ArrayList<>())
                            .build());
                }
            }
        }

        // Build BehaviorModel from B-能力 and E2
        List<OntologyExchangeDocument.Action> actions = new ArrayList<>();
        List<OntologyExchangeDocument.StateMachine> stateMachines = new ArrayList<>();

        for (SectionData sec : parsed.sections()) {
            if (KIND_B.equals(sec.kind())) {
                for (Map<String, String> row : sec.rows()) {
                    actions.add(OntologyExchangeDocument.Action.builder()
                            .id(row.getOrDefault("id", UUID.randomUUID().toString()))
                            .name(row.getOrDefault("name", ""))
                            .description(row.getOrDefault("description", ""))
                            .parameters(new ArrayList<>())
                            .preConditions(new ArrayList<>())
                            .triggerPhrases(new ArrayList<>())
                            .build());
                }
            }
        }

        // Build RuleModel from E3
        List<OntologyExchangeDocument.Rule> rules = new ArrayList<>();
        for (SectionData sec : parsed.sections()) {
            if (KIND_E3.equals(sec.kind())) {
                for (Map<String, String> row : sec.rows()) {
                    rules.add(OntologyExchangeDocument.Rule.builder()
                            .id(row.getOrDefault("id", UUID.randomUUID().toString()))
                            .name(row.getOrDefault("name", ""))
                            .description(row.getOrDefault("description", ""))
                            .type("custom")
                            .build());
                }
            }
        }

        // Build EventModel from E4
        List<OntologyExchangeDocument.EventDefinition> events = new ArrayList<>();
        for (SectionData sec : parsed.sections()) {
            if (KIND_E4.equals(sec.kind())) {
                for (Map<String, String> row : sec.rows()) {
                    events.add(OntologyExchangeDocument.EventDefinition.builder()
                            .id(row.getOrDefault("id", UUID.randomUUID().toString()))
                            .name(row.getOrDefault("name", ""))
                            .description(row.getOrDefault("description", ""))
                            .trigger("manual")
                            .payload(new ArrayList<>())
                            .payloadFields(new ArrayList<>())
                            .build());
                }
            }
        }

        // Build GovernanceModel from E5
        List<OntologyExchangeDocument.GovernanceRole> roles = new ArrayList<>();
        for (SectionData sec : parsed.sections()) {
            if (KIND_E5.equals(sec.kind())) {
                for (Map<String, String> row : sec.rows()) {
                    roles.add(OntologyExchangeDocument.GovernanceRole.builder()
                            .id(row.getOrDefault("id", UUID.randomUUID().toString()))
                            .name(row.getOrDefault("name", ""))
                            .permissions(new ArrayList<>())
                            .build());
                }
            }
        }

        // Build DataSourcesModel from E8
        List<OntologyExchangeDocument.DataSource> sources = new ArrayList<>();
        for (SectionData sec : parsed.sections()) {
            if (KIND_E8.equals(sec.kind())) {
                for (Map<String, String> row : sec.rows()) {
                    sources.add(OntologyExchangeDocument.DataSource.builder()
                            .id(row.getOrDefault("id", UUID.randomUUID().toString()))
                            .name(row.getOrDefault("name", ""))
                            .type(row.getOrDefault("type", "api"))
                            .build());
                }
            }
        }

        // Build Metrics from E6
        List<OntologyExchangeDocument.BusinessMetric> businessMetrics = new ArrayList<>();
        for (SectionData sec : parsed.sections()) {
            if (KIND_E6.equals(sec.kind())) {
                for (Map<String, String> row : sec.rows()) {
                    businessMetrics.add(OntologyExchangeDocument.BusinessMetric.builder()
                            .id(row.getOrDefault("id", UUID.randomUUID().toString()))
                            .name(row.getOrDefault("name", ""))
                            .description(row.getOrDefault("description", ""))
                            .build());
                }
            }
        }

        // Build EpcModel from EPC
        List<OntologyExchangeDocument.EpcChain> epcChains = new ArrayList<>();
        for (SectionData sec : parsed.sections()) {
            if (KIND_EPC.equals(sec.kind())) {
                List<OntologyExchangeDocument.EpcNode> epcNodes = new ArrayList<>();
                int order = 1;
                for (Map<String, String> row : sec.rows()) {
                    epcNodes.add(OntologyExchangeDocument.EpcNode.builder()
                            .id(row.getOrDefault("id", UUID.randomUUID().toString()))
                            .name(row.getOrDefault("name", ""))
                            .description(row.getOrDefault("description", ""))
                            .sortOrder(order++)
                            .nodeType("PROCESS_STEP")
                            .build());
                }
                String epcChainId = projectId + "-epc-chain";
                epcChains.add(OntologyExchangeDocument.EpcChain.builder()
                        .id(epcChainId)
                        .name(parsed.projectName() + " EPC Chain")
                        .description("")
                        .chainType("STANDARD")
                        .nodes(epcNodes)
                        .edges(new ArrayList<>())
                        .build());
            }
        }

        String now = Instant.now().toString();

        OntologyExchangeDocument.DataModel dataModel = OntologyExchangeDocument.DataModel.builder()
                .id(projectId + "-data")
                .name(parsed.projectName() + " DataModel")
                .version("0.1.0")
                .entities(entities)
                .build();

        OntologyExchangeDocument.BehaviorModel behaviorModel = OntologyExchangeDocument.BehaviorModel.builder()
                .id(projectId + "-behavior")
                .name(parsed.projectName() + " Behavior")
                .version("0.1.0")
                .actions(actions)
                .stateMachines(stateMachines)
                .build();

        OntologyExchangeDocument.RuleModel ruleModel = OntologyExchangeDocument.RuleModel.builder()
                .id(projectId + "-rules")
                .name(parsed.projectName() + " Rules")
                .version("0.1.0")
                .rules(rules)
                .build();

        OntologyExchangeDocument.EventModel eventModel = OntologyExchangeDocument.EventModel.builder()
                .id(projectId + "-events")
                .name(parsed.projectName() + " Events")
                .version("0.1.0")
                .events(events)
                .build();

        OntologyExchangeDocument.GovernanceModel governanceModel = OntologyExchangeDocument.GovernanceModel.builder()
                .id(projectId + "-gov")
                .roles(roles)
                .build();

        OntologyExchangeDocument.DataSourcesModel dataSourcesModel = OntologyExchangeDocument.DataSourcesModel.builder()
                .sources(sources)
                .build();

        OntologyExchangeDocument.MetricsModel metricsModel = OntologyExchangeDocument.MetricsModel.builder()
                .metrics(businessMetrics)
                .build();

        OntologyExchangeDocument.EpcModel epcModel = OntologyExchangeDocument.EpcModel.builder()
                .chains(epcChains)
                .profiles(new ArrayList<>())
                .build();

        String domainDescription = (domain != null) ? domain.getDescription() : "";
        return OntologyExchangeDocument.builder()
                .apiVersion("ontology.platform/v2")
                .kind("OntologyExchange")
                .metadata(OntologyExchangeDocument.Metadata.builder()
                        .id(projectId)
                        .version("0.1.0")
                        .name(parsed.projectName())
                        .description(domainDescription)
                        .source("markdown-import")
                        .status("draft")
                        .projectId(projectId)
                        .exportedAt(now)
                        .build())
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(OntologyExchangeDocument.OntologyProject.builder()
                                .id(projectId)
                                .name(parsed.projectName())
                                .description(domainDescription)
                                .domain(domain)
                                .dataModel(dataModel)
                                .behaviorModel(behaviorModel)
                                .ruleModel(ruleModel)
                                .eventModel(eventModel)
                                .governanceModel(governanceModel)
                                .dataSourcesModel(dataSourcesModel)
                                .metricsModel(metricsModel)
                                .epcModel(epcModel)
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

    // ======================== Parser ========================

    /**
     * Parse raw Markdown text into structured sections.
     */
    public MarkdownParseResult parseMarkdown(String text) {
        String[] lines = text.split("\n", -1);
        String projectName = "本体模型";
        String exportedAt = null;
        List<SectionData> sections = new ArrayList<>();

        // Extract title and export time from header
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("# ") && !trimmed.startsWith("## ")) {
                projectName = trimmed.substring(2).trim();
            } else if (trimmed.startsWith("> 导出时间:")) {
                exportedAt = trimmed.replace("> 导出时间:", "").trim();
            }
        }

        // Parse sections
        String currentKind = null;
        String currentTitle = null;
        List<String> currentHeaders = null;
        List<Map<String, String>> currentRows = new ArrayList<>();
        boolean inTable = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            // Section header
            Matcher matcher = SECTION_PATTERN.matcher(line);
            if (matcher.matches()) {
                // Flush previous section
                if (currentKind != null && !currentRows.isEmpty()) {
                    sections.add(new SectionData(currentKind, currentTitle, currentHeaders, currentRows));
                }
                currentHeaders = null;
                currentRows = new ArrayList<>();
                inTable = false;

                String rawTitle = matcher.group(1).trim().toLowerCase().replaceAll("\\s+", "");
                currentKind = resolveKind(rawTitle);
                currentTitle = matcher.group(1).trim();
                continue;
            }

            // Skip non-table lines when not in a section
            if (currentKind == null) continue;

            // Detect table start
            if (!inTable && trimmed.startsWith("|") && trimmed.contains("|")) {
                // Check if this is a header row (has | --- | pattern next line)
                if (i + 1 < lines.length) {
                    String nextLine = lines[i + 1].trim();
                    if (nextLine.startsWith("|") && nextLine.contains("-") && nextLine.contains("---")) {
                        // Header row
                        currentHeaders = parseTableRow(trimmed);
                        i++; // skip separator row
                        inTable = true;
                        continue;
                    }
                }
            }

            // Parse data rows
            if (inTable) {
                if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                    List<String> cells = parseTableRow(trimmed);
                    if (currentHeaders != null && !cells.isEmpty()) {
                        Map<String, String> row = new LinkedHashMap<>();
                        for (int j = 0; j < currentHeaders.size() && j < cells.size(); j++) {
                            // Normalize column keys: lower-case, no special chars
                            String key = normalizeHeader(currentHeaders.get(j));
                            row.put(key, cells.get(j));
                        }
                        currentRows.add(row);
                    }
                } else if (trimmed.isEmpty()) {
                    // End of table section
                    inTable = false;
                } else {
                    // Non-table content after table = end
                    inTable = false;
                }
            }
        }

        // Flush last section
        if (currentKind != null && !currentRows.isEmpty()) {
            sections.add(new SectionData(currentKind, currentTitle, currentHeaders, currentRows));
        }

        return new MarkdownParseResult(sections, projectName, exportedAt);
    }

    /**
     * Resolve a normalized section title to a kind string.
     */
    private String resolveKind(String normalized) {
        // Try exact match first, then prefix match
        String exact = SECTION_PREFIX_MAP.get(normalized);
        if (exact != null) return exact;

        for (Map.Entry<String, String> entry : SECTION_PREFIX_MAP.entrySet()) {
            if (normalized.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return normalized.toUpperCase();
    }

    /**
     * Parse a Markdown table row into cell values.
     */
    private List<String> parseTableRow(String line) {
        List<String> cells = new ArrayList<>();
        String content = line.trim();
        if (content.startsWith("|")) content = content.substring(1);
        if (content.endsWith("|")) content = content.substring(0, content.length() - 1);
        for (String cell : content.split("\\|")) {
            cells.add(cell.trim());
        }
        return cells;
    }

    /**
     * Normalize a Markdown table header to a property key.
     * E.g. "父ID" -> "parentId", "ID" -> "id", "名称" -> "name"
     */
    private String normalizeHeader(String header) {
        String h = header.trim().toLowerCase();
        return switch (h) {
            case "id" -> "id";
            case "名称" -> "name";
            case "描述" -> "description";
            case "父id", "父id " -> "parentId";
            case "类型" -> "type";
            default -> h.replaceAll("[\\s-]", "_");
        };
    }
}
