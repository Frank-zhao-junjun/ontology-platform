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
public class BusinessTermResult {
    private String id;
    private String name;
    private String nameEn;
    private String definition;
    private List<String> synonyms;
}
