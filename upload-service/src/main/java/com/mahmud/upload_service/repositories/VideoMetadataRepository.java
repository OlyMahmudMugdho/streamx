package com.mahmud.upload_service.repositories;

import com.mahmud.upload_service.models.VideoMetadata;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface VideoMetadataRepository extends ReactiveMongoRepository<VideoMetadata, String> {
}