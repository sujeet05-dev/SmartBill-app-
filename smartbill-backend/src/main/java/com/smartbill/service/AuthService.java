package com.smartbill.service;

import com.smartbill.dto.AuthResponse;
import com.smartbill.dto.LoginRequest;
import com.smartbill.dto.RegisterRequest;
import com.smartbill.entity.Role;
import com.smartbill.entity.User;
import com.smartbill.repository.UserRepository;
import com.smartbill.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        String cleanEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.findByEmailIgnoreCase(cleanEmail).isPresent()) {
            throw new RuntimeException("Email is already registered. Please sign in.");
        }

        User user = new User(
                cleanEmail,
                passwordEncoder.encode(request.getPassword()),
                Role.ADMIN
        );

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, user.getEmail(), user.getRole().name());
    }

    public AuthResponse authenticate(LoginRequest request) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email or username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        String cleanEmail = email.trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        cleanEmail,
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + cleanEmail));

        String jwtToken = jwtService.generateToken(user);
        
        return new AuthResponse(jwtToken, user.getEmail(), user.getRole().name());
    }
}
