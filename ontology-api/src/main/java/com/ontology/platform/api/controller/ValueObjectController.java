package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.CreateValueObjectRequest;
import com.ontology.platform.api.dto.ValueObjectResponse;
import com.ontology.platform.application.service.ValueObjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/value-objects")
@RequiredArgsConstructor
public class ValueObjectController {
    private final ValueObjectService voService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateValueObjectRequest req) {
        var vo = voService.create(req.getName(), req.getCode(), req.getNameEn(),
                req.getDescription(), req.getPropertiesJson());
        return ResponseEntity.status(HttpStatus.CREATED).body(ValueObjectResponse.toMap(vo));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listAll() {
        List<Map<String, Object>> list = voService.listAll().stream()
                .map(ValueObjectResponse::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        var vo = voService.getById(id);
        return ResponseEntity.ok(ValueObjectResponse.toMap(vo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String id, @RequestBody Map<String, String> body) {
        var vo = voService.update(id, body.get("name"), body.get("nameEn"),
                body.get("description"), body.get("propertiesJson"));
        return ResponseEntity.ok(ValueObjectResponse.toMap(vo));
    }
}
