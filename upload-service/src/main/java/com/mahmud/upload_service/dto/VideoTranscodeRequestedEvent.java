package com.mahmud.upload_service.dto;

public record VideoTranscodeRequestedEvent(
    String videoId,
    String title,
    String filename,
    String blobUrl,
    String contentType,
    long fileSize,
    String uploadDate
) {
}
