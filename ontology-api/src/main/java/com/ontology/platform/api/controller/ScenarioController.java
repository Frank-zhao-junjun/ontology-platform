package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.BusinessScenarioResponse;
import com.ontology.platform.api.dto.CreateBusinessScenarioRequest;
import com.ontology.platform.application.service.ScenarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/contexts/{contextId}/scenarios")
@RequiredArgsConstructor
public class ScenarioController {
    private final ScenarioService scenarioService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable String contextId,
            @Valid @RequestBody CreateBusinessScenarioRequest req) {
        var s = scenarioService.createScenario(contextId, req.getName(), req.getCode(),
                req.getNameEn(), req.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(BusinessScenarioResponse.toMap(s));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(@PathVariable String contextId) {
        List<Map<String, Object>> list = scenarioService.listScenarios(contextId).stream()
                .map(BusinessScenarioResponse::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{scenarioId}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String scenarioId,
            @RequestBody Map<String, String> body) {
        var s = scenarioService.updateScenario(scenarioId,
                body.get("name"), body.get("nameEn"), body.get("description"));
        return ResponseEntity.ok(BusinessScenarioResponse.toMap(s));
    }

    @PutMapping("/{scenarioId}/applicable-object-types")
    public ResponseEntity<Map<String, Object>> setApplicableObjectTypes(
            @PathVariable String scenarioId,
            @RequestBody Map<String, String> body) {
        var s = scenarioService.setApplicableObjectTypes(scenarioId, body.get("objectTypeIds"));
        return ResponseEntity.ok(BusinessScenarioResponse.toMap(s));
    }
}
