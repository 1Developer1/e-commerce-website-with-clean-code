package com.ecommerce.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final String secret;
    private final long expirationTimeMillis;

    public JwtUtil(
            @Value("${jwt.secret:default-very-secret-key-1234567890}") String secret,
            @Value("${jwt.expiration:3600000}") long expirationTimeMillis) {
        this.secret = secret;
        this.expirationTimeMillis = expirationTimeMillis;
    }

    public String generateToken(UUID userId) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withSubject(userId.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTimeMillis))
                .sign(algorithm);
    }

    public UUID validateTokenAndGetUserId(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return UUID.fromString(decodedJWT.getSubject());
        } catch (JWTVerificationException | IllegalArgumentException e) {
            return null; // Invalid token
        }
    }
}
