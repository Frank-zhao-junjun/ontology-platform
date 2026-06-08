package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.CreateStateMachineRequest;
import com.ontology.platform.api.dto.StateMachineResponse;
import com.ontology.platform.application.service.StateMachineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/contexts/{contextId}/state-machines")
@RequiredArgsConstructor
public class StateMachineController {
    private final StateMachineService smService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable String contextId,
            @Valid @RequestBody CreateStateMachineRequest req) {
        var sm = smService.create(contextId, req.getName(), req.getNameEn(),
                req.getObjectTypeId(), req.getStatusField(),
                req.getStatesJson(), req.getTransitionsJson());
        return ResponseEntity.status(HttpStatus.CREATED).body(StateMachineResponse.toMap(sm));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listByContext(@PathVariable String contextId) {
        List<Map<String, Object>> list = smService.listByContext(contextId).stream()
                .map(StateMachineResponse::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        var sm = smService.getById(id);
        return ResponseEntity.ok(StateMachineResponse.toMap(sm));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String id, @RequestBody Map<String, String> body) {
        var sm = smService.update(id, body.get("name"), body.get("nameEn"),
                body.get("statesJson"), body.get("transitionsJson"));
        return ResponseEntity.ok(StateMachineResponse.toMap(sm));
    }

    @GetMapping("/by-object-type/{objectTypeId}")
    public ResponseEntity<List<Map<String, Object>>> listByObjectType(
            @PathVariable String objectTypeId) {
        List<Map<String, Object>> list = smService.listByObjectType(objectTypeId).stream()
                .map(StateMachineResponse::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
