package com.shorturl.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> err = new HashMap<>();
        err.put("error", e.getMessage() != null ? e.getMessage() : "服务器内部错误");
        return ResponseEntity.status(500).body(err);
    }
}
