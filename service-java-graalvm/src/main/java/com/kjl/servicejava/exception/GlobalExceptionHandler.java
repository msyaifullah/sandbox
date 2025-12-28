package com.kjl.servicejava.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidOrderTypeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOrderType(InvalidOrderTypeException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", e.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<Map<String, Object>> handleOrderValidation(OrderValidationException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", e.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal server error");
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
