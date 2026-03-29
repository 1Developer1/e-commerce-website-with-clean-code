package com.ecommerce.user.usecase;

import com.ecommerce.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterUserOutput execute(RegisterUserInput input) {
        if (input.email() == null || input.email().isBlank()) {
            return new RegisterUserOutput(false, "Email is required", null, null);
        }
        if (input.password() == null || input.password().length() < 8) {
            return new RegisterUserOutput(false, "Password must be at least 8 characters", null, null);
        }
        if (input.name() == null || input.name().isBlank()) {
            return new RegisterUserOutput(false, "Name is required", null, null);
        }
        if (userRepository.existsByEmail(input.email())) {
            return new RegisterUserOutput(false, "Email already in use", null, null);
        }

        String passwordHash = passwordEncoder.encode(input.password());
        User user = User.create(input.email(), passwordHash, input.name());
        userRepository.save(user);

        return new RegisterUserOutput(true, "User registered successfully", user.getId().toString(), user.getEmail());
    }
}
