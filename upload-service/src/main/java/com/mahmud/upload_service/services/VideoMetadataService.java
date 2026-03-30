package com.mahmud.upload_service.services;

import com.mahmud.upload_service.models.VideoMetadata;
import com.mahmud.upload_service.repositories.VideoMetadataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class VideoMetadataService {

  @Autowired
  private VideoMetadataRepository repository;

  public Mono<VideoMetadata> saveMetadata(String filename, String originalFilename, String blobUrl, String contentType, long fileSize) {
    String title = generateTitle(originalFilename);
    LocalDateTime uploadDate = LocalDateTime.now();

    VideoMetadata metadata = new VideoMetadata();
    metadata.setFilename(filename);
    metadata.setBlobUrl(blobUrl);
    metadata.setContentType(contentType);
    metadata.setFileSize(fileSize);
    metadata.setTitle(title);
    metadata.setDuration(0);
    metadata.setUploadDate(uploadDate);

    return repository.save(metadata);
  }

  private String generateTitle(String filename) {
    int dotIndex = filename.lastIndexOf('.');
    String nameWithoutExt = (dotIndex > 0) ? filename.substring(0, dotIndex) : filename;
    if (nameWithoutExt.isEmpty()) {
      return "Video";
    }
    return nameWithoutExt.substring(0, 1).toUpperCase() + nameWithoutExt.substring(1);
  }
}
