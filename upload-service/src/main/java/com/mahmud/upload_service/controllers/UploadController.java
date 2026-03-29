package com.mahmud.upload_service.controllers;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.ParallelTransferOptions;
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

@RestController
@RequestMapping("blob")
public class UploadController {

  @Autowired
  private BlobServiceAsyncClient blobServiceAsyncClient;

  private static final String CONTAINER = "default";

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<String> uploadFile(@RequestPart("file") FilePart filePart) {

    // Validation: Only accept video MIME types
    String contentType = filePart.headers().getContentType() != null
        ? filePart.headers().getContentType().toString()
        : "";

    if (!contentType.startsWith("video/")) {
      return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only video files are allowed!"));
    }

    // Reference the specific blob client
    var blobAsyncClient = blobServiceAsyncClient
        .getBlobContainerAsyncClient(CONTAINER)
        .getBlobAsyncClient(filePart.filename());

    Flux<ByteBuffer> byteBufferFlux = filePart.content()
        .map(dataBuffer -> {
          // Create a byte array and read the buffer content into it
          byte[] bytes = new byte[dataBuffer.readableByteCount()];
          dataBuffer.read(bytes);

          // Release the original DataBuffer (Crucial for Netty)
          DataBufferUtils.release(dataBuffer);

          // Return a standard Java NIO ByteBuffer
          return ByteBuffer.wrap(bytes);
        });

    // Configure chunked upload settings
    ParallelTransferOptions options = new ParallelTransferOptions()
        .setBlockSizeLong(4L * 1024 * 1024) // 4MB Chunks
        .setMaxConcurrency(5);

    // Perform the upload and return the URI
    return blobAsyncClient.upload(byteBufferFlux, options, true)
        .thenReturn(blobAsyncClient.getBlobUrl());
  }
}
