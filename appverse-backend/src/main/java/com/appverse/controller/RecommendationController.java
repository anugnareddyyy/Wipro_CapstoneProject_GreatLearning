package com.appverse.controller;

import com.appverse.dto.response.AppResponse;
import com.appverse.repository.UserRepository;
import com.appverse.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for AI-powered recommendations and analytics.
 *
 * Base URL: /api/recommendations
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Recommendations", description = "Personalized app recommendations and predictive analytics")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    @GetMapping("/for-me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get personalized app recommendations for current user",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AppResponse>> getMyRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails.getUsername());
        log.info("Generating recommendations for user: {}", userId);
        return ResponseEntity.ok(recommendationService.getPersonalizedRecommendations(userId, limit));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending analysis with category breakdown")
    public ResponseEntity<Map<String, Object>> getTrendingAnalysis() {
        return ResponseEntity.ok(recommendationService.getTrendingAnalysis());
    }

    @GetMapping("/apps/{appId}/download-prediction")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "AI-powered 30-day download prediction for an app",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> predictDownloads(@PathVariable Long appId) {
        return ResponseEntity.ok(recommendationService.predictDownloads(appId));
    }

    @GetMapping("/category-insights")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Category growth insights (admin only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getCategoryInsights() {
        return ResponseEntity.ok(recommendationService.getCategoryInsights());
    }

    private Long getUserId(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getUserId();
    }
}
