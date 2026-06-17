package com.appverse.service;

import com.appverse.dto.request.LoginRequest;
import com.appverse.dto.request.RegisterRequest;
import com.appverse.dto.response.AuthResponse;

/**
 * Service interface for authentication operations.
 * Defines the contract for registration and login.
 */
public interface AuthService {

    /**
     * Register a new user account.
     *
     * @param request registration form data (username, email, password, role)
     * @return JWT token and user profile data
     * @throws com.appverse.exception.DuplicateResourceException if email or username already taken
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate a user and issue a JWT token.
     *
     * @param request login credentials (email + password)
     * @return JWT token and user profile data
     * @throws org.springframework.security.authentication.BadCredentialsException on bad credentials
     */
    AuthResponse login(LoginRequest request);
}
