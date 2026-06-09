package com.ontology.platform.domain.service.upload;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface DataFileParser {
    List<Map<String, String>> parseDataFile(InputStream inputStream, ParseConfig config);

    record ParseConfig(String fileType, boolean skipHeader, String encoding, int sheetIndex) {}
}
