package com.ecommerce.infrastructure.web;

import com.ecommerce.user.usecase.LoginInput;
import com.ecommerce.user.usecase.LoginOutput;
import com.ecommerce.user.usecase.LoginUseCase;
import com.ecommerce.user.usecase.RegisterUserInput;
import com.ecommerce.user.usecase.RegisterUserOutput;
import com.ecommerce.user.usecase.RegisterUserUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Auth", description = "Kullanıcı kaydı ve giriş işlemleri")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUseCase loginUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
    }

    public record RegisterRequest(
            @NotBlank(message = "Email is required") @Email(message = "Must be a valid email") String email,
            @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password,
            @NotBlank(message = "Name is required") String name) {}

    public record LoginRequest(
            @NotBlank(message = "Email is required") String email,
            @NotBlank(message = "Password is required") String password) {}

    @Operation(summary = "Yeni kullanıcı kaydı")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserOutput output = registerUserUseCase.execute(
                new RegisterUserInput(request.email(), request.password(), request.name()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", output.success());
        body.put("message", output.message());
        if (output.success()) {
            body.put("userId", output.userId());
            body.put("email", output.email());
        }

        HttpStatus status = output.success() ? HttpStatus.CREATED : HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(body);
    }

    @Operation(summary = "Kullanıcı girişi ve JWT token üretimi")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        LoginOutput output = loginUseCase.execute(new LoginInput(request.email(), request.password()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", output.success());
        body.put("message", output.message());
        if (output.success()) {
            body.put("token", output.token());
            body.put("userId", output.userId());
        }

        HttpStatus status = output.success() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(body);
    }
}
