package com.ontology.platform.domain.dto.semantic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentSlotResult {
    private String id;
    private String name;
    private String slotType;
    private Boolean required;
    private List<String> examples;
}
