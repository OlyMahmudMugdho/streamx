package com.mahmud.upload_service.exceptions;

import com.mahmud.upload_service.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(
      ResponseStatusException ex, ServerWebExchange exchange) {

    ErrorResponse error = new ErrorResponse(
        LocalDateTime.now(),
        ex.getStatusCode().value(),
        ex.getStatusCode().toString(),
        ex.getReason() != null ? ex.getReason() : "Action not allowed",
        exchange.getRequest().getPath().value());

    return new ResponseEntity<>(error, ex.getStatusCode());
  }

}
