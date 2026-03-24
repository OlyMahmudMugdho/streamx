package com.mahmud.upload_service.controllers;

import com.azure.storage.blob.BlobServiceAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("blob")
public class FileController {

  @Autowired
  private BlobServiceAsyncClient blobServiceAsyncClient;

  private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

  @GetMapping(value = "/fetch")
  public Mono<ResponseEntity<Flux<DataBuffer>>> downloadFile(@RequestParam("url") String blobUrl) {

    // 1. Parse URL to get container and blob name
    URI uri = URI.create(blobUrl);
    String[] parts = uri.getPath().substring(1).split("/", 2);
    String containerName = parts[0];
    String blobName = parts[1];

    var blobAsyncClient = blobServiceAsyncClient
        .getBlobContainerAsyncClient(containerName)
        .getBlobAsyncClient(blobName);

    // 2. Get properties first to set the correct Content-Length and Name
    return blobAsyncClient.getProperties().map(properties -> {

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
          // Force "Save As" dialogue with the original filename
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + blobName + "\"")
          .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(properties.getBlobSize()))
          .body(blobAsyncClient.downloadStream().map(bufferFactory::wrap));
    });
  }
}
