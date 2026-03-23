package com.ecommerce.infrastructure.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler — RFC 7807 (Problem Detail) formatında hata yanıtları üretir.
 * 
 * Güvenlik: Generic exception mesajları ASLA istemciye sızdırılmaz.
 * Sadece kontrollü Domain exception'ları (IllegalArgument/IllegalState) kullanıcıya gösterilir.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Domain katmanından gelen iş kuralı ihlalleri.
     * Bunlar kontrollü ve güvenli mesajlardır.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handleDomainExceptions(RuntimeException ex) {
        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Business Rule Violation",
                ex.getMessage()
        );
    }

    /**
     * Jakarta Bean Validation (JSR-380) hataları.
     * @Valid ile işaretli DTO'lardaki kural ihlallerinde tetiklenir.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<Map<String, String>> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> Map.of(
                        "field", fieldError.getField(),
                        "message", fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value"
                ))
                .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "https://api.ecommerce.com/errors/validation");
        body.put("title", "Validation Failed");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("detail", "One or more fields failed validation.");
        body.put("violations", violations);
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Beklenmeyen hatalar (NullPointer, DB bağlantı hataları vs.)
     * GÜVENLİK: ex.getMessage() ASLA dışarıya sızdırılmaz.
     * Sadece sunucu tarafında loglanır.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("[UNHANDLED_EXCEPTION] An unexpected error occurred", ex);

        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later."
        );
    }

    /**
     * RFC 7807 Problem Detail formatında standart hata zarfı oluşturur.
     */
    private ResponseEntity<Map<String, Object>> buildProblemDetail(HttpStatus status, String title, String detail) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "https://api.ecommerce.com/errors/" + title.toLowerCase().replace(" ", "-"));
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(status).body(body);
    }
}
