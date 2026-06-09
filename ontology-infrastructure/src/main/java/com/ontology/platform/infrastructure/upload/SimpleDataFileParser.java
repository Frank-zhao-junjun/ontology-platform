package com.ontology.platform.infrastructure.upload;

import com.ontology.platform.domain.service.upload.DataFileParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimpleDataFileParser implements DataFileParser {
    public List<Map<String, String>> parseDataFile(InputStream inputStream, ParseConfig config) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(config.encoding())))) {
            List<String> lines = reader.lines().toList();
            if (lines.isEmpty()) {
                return List.of();
            }
            String[] headers = lines.get(0).split(",");
            List<Map<String, String>> rows = new ArrayList<>();
            int start = config.skipHeader() ? 1 : 0;
            for (int i = start; i < lines.size(); i++) {
                String[] values = lines.get(i).split(",");
                Map<String, String> row = new LinkedHashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    row.put(headers[j], j < values.length ? values[j] : "");
                }
                rows.add(row);
            }
            return rows;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse data file", e);
        }
    }
}
