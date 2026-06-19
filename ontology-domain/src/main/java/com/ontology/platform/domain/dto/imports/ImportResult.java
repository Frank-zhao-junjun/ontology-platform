package com.ontology.platform.domain.dto.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果 — 通用容器
 *
 * @param <T> 导入实体的类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult<T> {

    /** 成功创建的实体列表 */
    @Builder.Default
    private List<T> imported = new ArrayList<>();

    /** 因冲突跳过的记录（含原因） */
    @Builder.Default
    private List<SkipEntry> skipped = new ArrayList<>();

    /** 解析失败的记录（含原因） */
    @Builder.Default
    private List<ErrorEntry> errors = new ArrayList<>();

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public int totalProcessed() {
        return imported.size() + skipped.size() + errors.size();
    }

    public void addImported(T entity) {
        imported.add(entity);
    }

    public void addSkipped(String id, String reason) {
        skipped.add(new SkipEntry(id, reason));
    }

    public void addError(int row, String field, String message) {
        errors.add(new ErrorEntry(row, field, message));
    }

    @Data
    @AllArgsConstructor
    public static class SkipEntry {
        private String id;
        private String reason;
    }

    @Data
    @AllArgsConstructor
    public static class ErrorEntry {
        private int row;
        private String field;
        private String message;
    }
}
