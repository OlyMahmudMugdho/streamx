package com.mahmud.upload_service.services;

import com.mahmud.upload_service.dto.VideoTranscodeRequestedEvent;
import com.mahmud.upload_service.models.VideoMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class VideoTranscodeEventPublisher {

  private final KafkaTemplate<Object, Object> kafkaTemplate;
  private final String topic;

  public VideoTranscodeEventPublisher(
      KafkaTemplate<Object, Object> kafkaTemplate,
      @Value("${app.kafka.video-transcode-topic}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  public Mono<Void> publish(VideoMetadata metadata) {
    VideoTranscodeRequestedEvent event = new VideoTranscodeRequestedEvent(
        metadata.getId(),
        metadata.getTitle(),
        metadata.getFilename(),
        metadata.getBlobUrl(),
        metadata.getContentType(),
        metadata.getFileSize(),
        metadata.getUploadDate() != null ? metadata.getUploadDate().toString() : null);

    return Mono.fromFuture(kafkaTemplate.send(topic, metadata.getId(), event)).then();
  }
}
