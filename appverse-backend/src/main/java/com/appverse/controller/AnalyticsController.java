package com.appverse.controller;
 
import com.appverse.repository.UserRepository;
import com.appverse.service.AnalyticsService;
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
 
import java.util.Map;
 
/**
 * REST controller for Developer Analytics Dashboard (Module 6).
 *
 * Base URL: /api/analytics
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Developer analytics dashboard: downloads, revenue, engagement")
public class AnalyticsController {
 
    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;
 
    @GetMapping("/developer")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Get complete analytics dashboard for current developer",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getDeveloperAnalytics(
            @AuthenticationPrincipal UserDetails userDetails) {
 
        Long developerId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUserId();
 
        log.info("GET /api/analytics/developer - developer: {}", developerId);
        return ResponseEntity.ok(analyticsService.getDeveloperAnalytics(developerId));
    }
}
 