package com.ontology.platform.api.controller;

import com.ontology.platform.application.service.DataSourceService;
import com.ontology.platform.domain.entity.DataAccessMethod;
import com.ontology.platform.domain.entity.DataSource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "DataSources", description = "数据源与获取方式 (US-S08)")
public class DataSourceController {
    private final DataSourceService svc;

    @PostMapping("/data-sources")
    @Operation(summary = "配置数据源 (US-S08)")
    public ResponseEntity<Map<String, Object>> createDataSource(@RequestBody Map<String, Object> body) {
        DataSource ds = svc.createDataSource(
                str(body, "name"), str(body, "code"), str(body, "sourceType"),
                body.get("connectionConfig") != null ? body.get("connectionConfig").toString() : "{}",
                str(body, "credentialRef"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toDsMap(ds)));
    }

    @GetMapping("/data-sources")
    public ResponseEntity<Map<String, Object>> listDataSources(@RequestParam(required = false) String sourceType) {
        var data = svc.listDataSources(sourceType).stream().map(this::toDsMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("code", 0, "message", "success", "data", data));
    }

    @PostMapping("/contexts/{contextId}/data-access-methods")
    @Operation(summary = "配置数据获取方式 (US-S08)")
    public ResponseEntity<Map<String, Object>> createAccessMethod(@PathVariable String contextId,
                                                                  @RequestBody Map<String, Object> body) {
        int ttl = body.get("cacheTtlSec") instanceof Number n ? n.intValue() : 300;
        DataAccessMethod m = svc.createDataAccessMethod(contextId,
                str(body, "objectTypeId"), str(body, "dataSourceId"), str(body, "methodType"),
                body.get("accessConfig") != null ? body.get("accessConfig").toString() : "{}", ttl);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("code", 0, "message", "success", "data", toMethodMap(m)));
    }

    private Map<String, Object> toDsMap(DataSource ds) {
        return Map.of("id", ds.getId(), "name", ds.getName(), "code", ds.getCode(),
                "sourceType", ds.getSourceType(), "connectionConfig", ds.getConnectionConfig(),
                "credentialRef", ds.getCredentialRef() != null ? ds.getCredentialRef() : "");
    }

    private Map<String, Object> toMethodMap(DataAccessMethod m) {
        return Map.of("id", m.getId(), "contextId", m.getContextId(), "objectTypeId", m.getObjectTypeId(),
                "dataSourceId", m.getDataSourceId(), "methodType", m.getMethodType(),
                "accessConfig", m.getAccessConfig(), "cacheTtlSec", m.getCacheTtlSec());
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v != null ? v.toString() : null;
    }
}
