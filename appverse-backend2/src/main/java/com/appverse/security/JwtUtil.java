package com.appverse.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility component for JWT (JSON Web Token) operations.
 *
 * Responsibilities:
 * - Generate signed JWT tokens on successful authentication
 * - Extract claims (username, roles, expiry) from tokens
 * - Validate tokens against the stored user details
 *
 * Algorithm: HMAC-SHA256 (HS256)
 * Token lifetime is configurable via application.properties.
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Generate a JWT token for the authenticated user.
     * Includes username and role as claims for downstream authorization checks.
     *
     * @param userDetails the Spring Security user details of the logged-in user
     * @return signed JWT string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        return buildToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Generate a JWT with additional custom claims.
     *
     * @param extraClaims additional key-value pairs to embed in the token payload
     * @param userDetails the authenticated user
     * @return signed JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Internal builder that constructs and signs the JWT.
     */
    private String buildToken(Map<String, Object> extraClaims, String subject, Long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate a token against the current user's details.
     * Checks both username match and token expiry.
     *
     * @param token       JWT string from Authorization header
     * @param userDetails loaded user details for the subject
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract the username (email) from the token subject claim.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the expiration date from the token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from the token using a resolver function.
     *
     * @param token          JWT string
     * @param claimsResolver function that extracts the desired value from Claims
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse and return all claims from the token.
     * Throws a JwtException if the token is tampered with or expired.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check whether the token's expiry date is in the past.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Decode the Base64-encoded secret and return the HMAC signing key.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(secretKey.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get the configured token expiration time in milliseconds.
     */
    public Long getExpirationTime() {
        return jwtExpiration;
    }
}
