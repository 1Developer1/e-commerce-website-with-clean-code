package com.ecommerce.adapter.in.web;

import com.ecommerce.infrastructure.security.JwtUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Mock Login Endpoint to generate tokens easily for testing.
    // In a real application, this would validate credentials first via an AuthUseCase.
    @PostMapping("/login")
    public Map<String, String> login() {
        // Mocking a successful login always returning a fixed mock UUID for simplicity in testing
        UUID mockUserId = UUID.fromString("e49d60ea-5247-41ec-b2c6-1e6634123512");
        String token = jwtUtil.generateToken(mockUserId);
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", mockUserId.toString());
        return response;
    }
}
