package com.ecommerce.user.usecase;

import com.ecommerce.user.entity.User;
import com.ecommerce.user.usecase.port.TokenGeneratorPort;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGeneratorPort tokenGenerator;

    public LoginUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenGeneratorPort tokenGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    public LoginOutput execute(LoginInput input) {
        if (input.email() == null || input.email().isBlank()) {
            return new LoginOutput(false, "Email is required", null, null);
        }
        if (input.password() == null || input.password().isBlank()) {
            return new LoginOutput(false, "Password is required", null, null);
        }

        User user = userRepository.findByEmail(input.email()).orElse(null);
        if (user == null || !passwordEncoder.matches(input.password(), user.getPasswordHash())) {
            return new LoginOutput(false, "Invalid email or password", null, null);
        }

        String token = tokenGenerator.generateToken(user.getId());
        return new LoginOutput(true, "Login successful", token, user.getId().toString());
    }
}
