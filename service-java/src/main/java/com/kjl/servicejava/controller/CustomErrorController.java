package com.kjl.servicejava.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> handleError(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "this service is ok :)");
        
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            try {
                int statusCode = Integer.parseInt(status.toString());
                HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                return ResponseEntity.status(httpStatus).body(response);
            } catch (Exception e) {
                // If status parsing fails, return OK
            }
        }
        
        return ResponseEntity.ok(response);
    }
}
