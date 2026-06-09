package com.ontology.platform.domain.service.upload;

public interface FileStorageService {
    void initChunkDirectory(String uploadId);
    void saveChunk(String uploadId, int chunkNumber, byte[] chunkData);
    String mergeChunks(String uploadId, int totalChunks, String fileName);
    String calculateMd5(String filePath);
    int deleteChunks(String uploadId);
}
