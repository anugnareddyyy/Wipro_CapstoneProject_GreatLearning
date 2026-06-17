package com.appverse.service.impl;

import com.appverse.dto.request.LoginRequest;
import com.appverse.dto.request.RegisterRequest;
import com.appverse.dto.response.AuthResponse;
import com.appverse.entity.Role;
import com.appverse.entity.User;
import com.appverse.exception.DuplicateResourceException;
import com.appverse.repository.UserRepository;
import com.appverse.security.JwtUtil;
import com.appverse.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService handling user registration and login.
 *
 * Registration flow:
 * 1. Validate email and username uniqueness
 * 2. Hash the password with BCrypt
 * 3. Persist the User entity
 * 4. Generate and return a JWT
 *
 * Login flow:
 * 1. Authenticate credentials via Spring Security's AuthenticationManager
 * 2. Generate and return a JWT on success
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Validate uniqueness before proceeding
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                "An account with email '" + request.getEmail() + "' already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                "Username '" + request.getUsername() + "' is already taken");
        }

        // Determine role (default to USER if not specified or invalid)
        Role role = Role.USER;
        if ("DEVELOPER".equalsIgnoreCase(request.getRole())) {
            role = Role.DEVELOPER;
        }

        // Build and persist the user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(role)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: id={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        // Generate JWT for immediate login after registration
        UserDetails userDetails = buildUserDetails(savedUser);
        String token = jwtUtil.generateToken(userDetails);

        return buildAuthResponse(savedUser, token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Delegate credential verification to Spring Security
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, token);
    }

    /**
     * Build a minimal Spring Security UserDetails from our User entity.
     * Used to generate JWT immediately after registration.
     */
    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    /**
     * Map User entity and JWT string to AuthResponse DTO.
     */
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
