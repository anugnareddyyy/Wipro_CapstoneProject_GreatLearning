package com.appverse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO returned on successful authentication (login or register).
 * Contains the JWT token and minimal user profile data for the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** Bearer token to include in Authorization header for all protected requests. */
    private String token;

    /** Token type (always "Bearer"). */
    @Builder.Default
    private String tokenType = "Bearer";

    /** How long the token is valid in milliseconds. */
    private Long expiresIn;

    private Long userId;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String avatarUrl;
}
