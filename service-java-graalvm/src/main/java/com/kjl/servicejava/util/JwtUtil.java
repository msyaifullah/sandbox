package com.kjl.servicejava.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    public String createJWT() throws Exception {
        PrivateKey privateKey = loadPrivateKey(privateKeyPath);
        
        return Jwts.builder()
            .subject("service-b-user")
            .issuer("service-b")
            .audience().add("service-a").and()
            .expiration(Date.from(Instant.now().plusSeconds(30 * 60))) // 30 minutes
            .issuedAt(Date.from(Instant.now()))
            .signWith(privateKey)
            .compact();
    }

    public Claims verifyToken(String tokenString) throws Exception {
        PublicKey publicKey = loadPublicKey(publicKeyPath);
        
        return Jwts.parser()
            .verifyWith(publicKey)
            .requireIssuer("service-a")
            .requireAudience("service-b")
            .build()
            .parseSignedClaims(tokenString)
            .getPayload();
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        try {
            String keyContent = new String(Files.readAllBytes(Paths.get(path)));
            // Handle both PKCS8 and PKCS1 formats
            keyContent = keyContent.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new Exception("Failed to load private key: " + e.getMessage(), e);
        }
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        try {
            String keyContent = new String(Files.readAllBytes(Paths.get(path)));
            keyContent = keyContent.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new Exception("Failed to load public key: " + e.getMessage(), e);
        }
    }
}
