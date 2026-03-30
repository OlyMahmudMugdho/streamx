package com.mahmud.upload_service.controllers;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.mahmud.upload_service.dto.UploadResponse;
import com.mahmud.upload_service.services.VideoMetadataService;
import com.mahmud.upload_service.services.VideoTranscodeEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.UUID;

@RestController
@RequestMapping("blob")
public class UploadController {

  @Autowired
  private BlobServiceAsyncClient blobServiceAsyncClient;

  @Autowired
  private VideoMetadataService videoMetadataService;

  @Autowired
  private VideoTranscodeEventPublisher videoTranscodeEventPublisher;

  private static final String CONTAINER = "default";

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<UploadResponse> uploadFile(@RequestPart("file") FilePart filePart) {
    String originalFilename = filePart.filename();
    String storedFilename = buildStoredFilename(originalFilename);

    String contentType = filePart.headers().getContentType() != null
        ? filePart.headers().getContentType().toString()
        : "";

    if (!contentType.startsWith("video/")) {
      return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only video files are allowed!"));
    }

    var blobAsyncClient = blobServiceAsyncClient
        .getBlobContainerAsyncClient(CONTAINER)
        .getBlobAsyncClient(storedFilename);

    long[] actualSize = {0};
    Flux<ByteBuffer> byteBufferFlux = filePart.content()
        .map(dataBuffer -> {
          byte[] bytes = new byte[dataBuffer.readableByteCount()];
          actualSize[0] += bytes.length;
          dataBuffer.read(bytes);
          DataBufferUtils.release(dataBuffer);
          return ByteBuffer.wrap(bytes);
        });

    ParallelTransferOptions options = new ParallelTransferOptions()
        .setBlockSizeLong(4L * 1024 * 1024)
        .setMaxConcurrency(5);

    String blobUrl = blobAsyncClient.getBlobUrl();

    return blobAsyncClient.upload(byteBufferFlux, options, true)
        .flatMap(ignored -> videoMetadataService.saveMetadata(storedFilename, originalFilename, blobUrl, contentType, actualSize[0]))
        .flatMap(savedMetadata -> videoTranscodeEventPublisher.publish(savedMetadata)
            .thenReturn(savedMetadata))
        .map(metadata -> new UploadResponse(
            metadata.getId(),
            metadata.getFilename(),
            metadata.getBlobUrl(),
            metadata.getContentType(),
            metadata.getFileSize(),
            metadata.getUploadDate()));
  }

  private String buildStoredFilename(String originalFilename) {
    int dotIndex = originalFilename.lastIndexOf('.');
    String extension = (dotIndex >= 0) ? originalFilename.substring(dotIndex) : "";
    return UUID.randomUUID() + extension;
  }
}
