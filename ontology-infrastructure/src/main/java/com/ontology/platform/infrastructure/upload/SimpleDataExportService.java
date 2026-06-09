package com.ontology.platform.infrastructure.upload;

import com.ontology.platform.domain.service.upload.DataExportService;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Service
public class SimpleDataExportService implements DataExportService {
    public void generateImportTemplateExcel(String objectTypeName, List<String> properties, OutputStream outputStream, String sheetName) {
        generateImportTemplateCsv(objectTypeName, properties, outputStream);
    }

    public void generateImportTemplateCsv(String objectTypeName, List<String> properties, OutputStream outputStream) {
        write(String.join(",", properties) + "\n", outputStream, "UTF-8");
    }

    public void exportToExcel(List<Map<String, Object>> data, List<String> headers, OutputStream outputStream, String sheetName) {
        exportToCsv(data, headers, outputStream, "UTF-8");
    }

    public void exportToCsv(List<Map<String, Object>> data, List<String> headers, OutputStream outputStream, String encoding) {
        StringBuilder csv = new StringBuilder(String.join(",", headers)).append('\n');
        for (Map<String, Object> row : data) {
            csv.append(headers.stream().map(h -> String.valueOf(row.getOrDefault(h, ""))).reduce((a, b) -> a + "," + b).orElse(""))
                    .append('\n');
        }
        write(csv.toString(), outputStream, encoding);
    }

    private void write(String value, OutputStream outputStream, String encoding) {
        try {
            outputStream.write(value.getBytes(Charset.forName(encoding)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write export data", e);
        }
    }
}
