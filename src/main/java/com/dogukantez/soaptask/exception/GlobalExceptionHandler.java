package com.dogukantez.soaptask.exception;

import com.dogukantez.soaptask.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {

        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Validation failed", "Request contains invalid fields", details);
    }

    @ExceptionHandler(ViesInvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(ViesInvalidInputException e) {
        log.warn("VIES rejected input: {}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Invalid input", e.getMessage(), null);
    }

    @ExceptionHandler(ViesTemporaryException.class)
    public ResponseEntity<ErrorResponse> handleTemporary(ViesTemporaryException e) {
        log.warn("VIES temporarily unavailable: {}", e.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable",
                "VIES is temporarily unavailable, please try again later", null);
    }

    @ExceptionHandler(ViesTechnicalException.class)
    public ResponseEntity<ErrorResponse> handleTechnical(ViesTechnicalException e) {
        log.error("VIES technical error", e);
        return build(HttpStatus.BAD_GATEWAY, "Upstream error",
                "VAT validation could not be completed", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error",
                "An unexpected error occurred", null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error,
                                                String message, List<String> details) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .details(details)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}