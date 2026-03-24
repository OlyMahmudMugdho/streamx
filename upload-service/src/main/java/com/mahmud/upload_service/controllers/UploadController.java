package com.mahmud.upload_service.controllers;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.ParallelTransferOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("blob")
public class UploadController {

  @Autowired
  private BlobServiceAsyncClient blobServiceAsyncClient;

  private static final String CONTAINER = "default";

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<String> uploadFile(@RequestPart("file") FilePart filePart) {

    // 1. Validation: Only accept video MIME types
    String contentType = filePart.headers().getContentType() != null
        ? filePart.headers().getContentType().toString()
        : "";

    if (!contentType.startsWith("video/")) {
      return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only video files are allowed!"));
    }

    // 2. Map the Flux<DataBuffer> to Flux<ByteBuffer> for the Azure SDK
    // We use .map(DataBuffer::asByteBuffer) to bridge Spring to Azure
    var byteBufferFlux = filePart.content()
        .map(DataBuffer::asByteBuffer);

    // 3. Configure chunked upload settings
    ParallelTransferOptions options = new ParallelTransferOptions()
        .setBlockSizeLong(4L * 1024 * 1024) // 4MB chunks
        .setMaxConcurrency(5); // Balanced concurrency for stability

    // 4. Perform the upload
    return blobServiceAsyncClient.getBlobContainerAsyncClient(CONTAINER)
        .getBlobAsyncClient(filePart.filename())
        .upload(byteBufferFlux, options, true)
        .thenReturn("Successfully uploaded video: " + filePart.filename());
  }
}
