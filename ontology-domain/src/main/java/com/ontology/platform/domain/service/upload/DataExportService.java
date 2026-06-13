package com.ontology.platform.domain.service.upload;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface DataExportService {
    void generateImportTemplateExcel(String objectTypeName, List<String> properties, OutputStream outputStream, String sheetName);
    void generateImportTemplateCsv(String objectTypeName, List<String> properties, OutputStream outputStream);
    void exportToExcel(List<Map<String, Object>> data, List<String> headers, OutputStream outputStream, String sheetName);
    void exportToCsv(List<Map<String, Object>> data, List<String> headers, OutputStream outputStream, String encoding);
}
