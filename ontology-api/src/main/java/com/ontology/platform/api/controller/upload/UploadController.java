package com.ontology.platform.api.controller.upload;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.upload.*;
import com.ontology.platform.application.service.upload.ExportService;
import com.ontology.platform.application.service.upload.ImportService;
import com.ontology.platform.application.service.upload.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

/**
 * 文件上传下载控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "大文件分片上传接口")
public class UploadController {

    private final UploadService uploadService;
    private final ImportService importService;
    private final ExportService exportService;

    // ==================== 上传相关 ====================

    /**
     * 初始化上传任务
     */
    @PostMapping("/uploads/init")
    @Operation(summary = "初始化上传", description = "初始化分片上传任务")
    public ApiResponse<UploadTaskResponse> initUpload(
            @Valid @RequestBody InitUploadRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "default") String userId,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {
        
        log.info("Init upload request: fileName={}, fileSize={}", 
                request.getFileName(), request.getFileSize());
        
        UploadTaskResponse response = uploadService.initUpload(request, userId, tenantId);
        return ApiResponse.success(response);
    }

    /**
     * 上传分片
     */
    @PutMapping("/uploads/{uploadId}/chunks/{chunkNumber}")
    @Operation(summary = "上传分片", description = "上传单个文件分片")
    public ApiResponse<UploadTaskResponse> uploadChunk(
            @PathVariable String uploadId,
            @PathVariable int chunkNumber,
            @RequestBody byte[] chunkData,
            @RequestHeader(value = "Content-MD5", required = false) String contentMd5) {
        
        log.debug("Upload chunk: uploadId={}, chunkNumber={}, size={}", 
                uploadId, chunkNumber, chunkData.length);
        
        UploadTaskResponse response = uploadService.uploadChunk(uploadId, chunkNumber, chunkData, contentMd5);
        return ApiResponse.success(response);
    }

    /**
     * 查询上传状态
     */
    @GetMapping("/uploads/{uploadId}")
    @Operation(summary = "查询上传状态", description = "获取上传任务进度")
    public ApiResponse<UploadTaskResponse> getUploadStatus(@PathVariable String uploadId) {
        UploadTaskResponse response = uploadService.getUploadStatus(uploadId);
        return ApiResponse.success(response);
    }

    /**
     * 完成上传
     */
    @PostMapping("/uploads/{uploadId}/complete")
    @Operation(summary = "完成上传", description = "完成分片上传并合并文件")
    public ApiResponse<UploadTaskResponse> completeUpload(
            @PathVariable String uploadId,
            @RequestBody(required = false) CompleteUploadRequest request) {
        
        String finalMd5 = request != null ? request.getFinalMd5() : null;
        UploadTaskResponse response = uploadService.completeUpload(uploadId, finalMd5);
        return ApiResponse.success(response);
    }

    /**
     * 取消上传
     */
    @DeleteMapping("/uploads/{uploadId}")
    @Operation(summary = "取消上传", description = "取消上传任务并清理分片")
    public ApiResponse<CancelUploadResponse> cancelUpload(@PathVariable String uploadId) {
        int deletedChunks = uploadService.cancelUpload(uploadId);
        return ApiResponse.success(new CancelUploadResponse(uploadId, true, deletedChunks));
    }

    // ==================== 导入相关 ====================

    /**
     * 执行导入
     */
    @PostMapping("/ontologies/{ontologyId}/import")
    @Operation(summary = "执行导入", description = "导入对象实例数据")
    public ApiResponse<ImportTaskResponse> executeImport(
            @PathVariable String ontologyId,
            @Valid @RequestBody ImportRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "default") String userId,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {
        
        request.setOntologyId(ontologyId);
        log.info("Execute import: ontologyId={}, objectType={}", ontologyId, request.getObjectTypeName());
        
        ImportTaskResponse response = importService.executeImport(request, userId, tenantId);
        return ApiResponse.success(response);
    }

    /**
     * 查询导入状态
     */
    @GetMapping("/imports/{importId}")
    @Operation(summary = "查询导入状态", description = "获取导入任务进度")
    public ApiResponse<ImportTaskResponse> getImportStatus(@PathVariable String importId) {
        ImportTaskResponse response = importService.getImportStatus(importId);
        return ApiResponse.success(response);
    }

    /**
     * 取消导入
     */
    @DeleteMapping("/imports/{importId}")
    @Operation(summary = "取消导入", description = "取消正在运行的导入任务")
    public ApiResponse<Void> cancelImport(@PathVariable String importId) {
        importService.cancelImport(importId);
        return ApiResponse.success(null);
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/ontologies/{ontologyId}/object-types/{objectTypeName}/import-template")
    @Operation(summary = "下载导入模板", description = "获取指定对象类型的导入模板")
    public ResponseEntity<byte[]> downloadImportTemplate(
            @PathVariable String ontologyId,
            @PathVariable String objectTypeName,
            @RequestParam(defaultValue = "csv") String format) {
        
        log.info("Download import template: objectType={}, format={}", objectTypeName, format);
        
        byte[] templateData = uploadService.getImportTemplate(ontologyId, objectTypeName, format);
        
        String contentType = "csv".equalsIgnoreCase(format) 
                ? "text/csv" 
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        
        String fileName = objectTypeName + "_import_template." + format;
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(templateData);
    }

    // ==================== 导出相关 ====================

    /**
     * 执行导出
     */
    @GetMapping("/ontologies/{ontologyId}/export")
    @Operation(summary = "执行导出", description = "导出对象实例数据")
    public void executeExport(
            @PathVariable String ontologyId,
            @RequestParam String objectTypeName,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String encoding,
            @RequestParam(required = false) Integer limit,
            @RequestHeader(value = "X-User-Id", defaultValue = "default") String userId,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId,
            HttpServletResponse response) throws IOException {
        
        log.info("Execute export: ontologyId={}, objectType={}, format={}", 
                ontologyId, objectTypeName, format);
        
        ExportRequest request = ExportRequest.builder()
                .ontologyId(ontologyId)
                .objectTypeName(objectTypeName)
                .format(format)
                .encoding(encoding)
                .limit(limit)
                .build();
        
        ExportService.ExportResult result = exportService.executeExport(request, userId, tenantId);
        
        String contentType = "csv".equalsIgnoreCase(format) 
                ? "text/csv; charset=UTF-8" 
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + result.fileName() + "\"");
        response.setContentType(contentType);
        response.getOutputStream().write(result.data());
        response.getOutputStream().flush();
    }

    /**
     * 流式导出（大数据量）
     */
    @GetMapping(value = "/ontologies/{ontologyId}/export/stream")
    @Operation(summary = "流式导出", description = "大文件流式导出，减少内存占用")
    public StreamingResponseBody streamExport(
            @PathVariable String ontologyId,
            @RequestParam String objectTypeName,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String encoding,
            @RequestHeader(value = "X-User-Id", defaultValue = "default") String userId,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {
        
        ExportRequest request = ExportRequest.builder()
                .ontologyId(ontologyId)
                .objectTypeName(objectTypeName)
                .format(format)
                .encoding(encoding)
                .build();
        
        return exportService.streamExport(request, userId, tenantId);
    }

    // ==================== 内部类 ====================

    @lombok.Getter
    @lombok.Setter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompleteUploadRequest {
        private String finalMd5;
    }

    public record CancelUploadResponse(String uploadId, boolean deleted, int cleanedChunks) {}
}
