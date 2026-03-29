package com.mahmud.upload_service.dto;

import java.time.LocalDateTime;

public record UploadResponse(
    String id,
    String filename,
    String blobUrl,
    String contentType,
    long fileSize,
    LocalDateTime uploadDate) {
}
