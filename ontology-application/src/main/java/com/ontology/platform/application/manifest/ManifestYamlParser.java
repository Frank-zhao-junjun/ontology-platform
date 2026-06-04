package com.ontology.platform.application.manifest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ManifestYamlParser {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public JsonNode parseYaml(String yaml) throws IOException {
        return yamlMapper.readTree(yaml);
    }

    public JsonNode parseJson(String json) throws IOException {
        return jsonMapper.readTree(json);
    }
}
