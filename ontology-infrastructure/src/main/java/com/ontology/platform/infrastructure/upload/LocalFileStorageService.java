package com.ontology.platform.infrastructure.upload;

import com.ontology.platform.domain.service.upload.FileStorageService;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Map<String, byte[]> files = new ConcurrentHashMap<>();

    public void initChunkDirectory(String uploadId) {}
    public void saveChunk(String uploadId, int chunkNumber, byte[] chunkData) { files.put(uploadId + ":" + chunkNumber, chunkData); }
    public String mergeChunks(String uploadId, int totalChunks, String fileName) { return uploadId + "/" + fileName; }
    public int deleteChunks(String uploadId) { int before = files.size(); files.keySet().removeIf(k -> k.startsWith(uploadId + ":")); return before - files.size(); }

    public String calculateMd5(String filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return HexFormat.of().formatHex(digest.digest(filePath.getBytes()));
        } catch (Exception e) {
            return "";
        }
    }
}
