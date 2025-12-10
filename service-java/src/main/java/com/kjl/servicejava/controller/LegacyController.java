package com.kjl.servicejava.controller;

import com.kjl.servicejava.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LegacyController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/api/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "missing token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String token = auth.substring(7);
        try {
            Claims claims = jwtUtil.verifyToken(token);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Hello from Service Java");
            response.put("by", claims.getSubject());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/call-a")
    public ResponseEntity<String> callA() {
        try {
            String token = jwtUtil.createJWT();
            // In a real implementation, you would make an HTTP call to service-a
            // For now, just return the token
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "failed to sign");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }
}
