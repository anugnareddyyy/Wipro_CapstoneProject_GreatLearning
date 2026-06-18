package com.appverse.controller;

import com.appverse.dto.request.AppRequest;
import com.appverse.dto.response.AppResponse;
import com.appverse.entity.AppCategory;
import com.appverse.entity.AppStatus;
import com.appverse.repository.UserRepository;
import com.appverse.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for App marketplace, developer console, and admin operations.
 *
 * Route structure:
 * - GET  /api/apps             -> Marketplace listing (PUBLIC)
 * - GET  /api/apps/search      -> Search (PUBLIC)
 * - GET  /api/apps/{id}        -> App detail (PUBLIC)
 * - GET  /api/apps/featured    -> Featured apps (PUBLIC)
 * - GET  /api/apps/trending    -> Trending apps (PUBLIC)
 * - POST /api/apps             -> Create app (DEVELOPER, ADMIN)
 * - PUT  /api/apps/{id}        -> Update app (DEVELOPER, ADMIN)
 * - DELETE /api/apps/{id}      -> Delete app (DEVELOPER, ADMIN)
 * - GET  /api/apps/my-apps     -> Developer's own apps (DEVELOPER, ADMIN)
 * - POST /api/apps/{id}/download -> Record download (USER, DEVELOPER, ADMIN)
 * - GET  /api/apps/admin/pending -> Pending apps (ADMIN)
 * - PATCH /api/apps/{id}/status  -> Update status (ADMIN)
 */
@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Apps", description = "App marketplace and developer console operations")
public class AppController {

    private final AppService appService;
    private final UserRepository userRepository;

    // ===== MARKETPLACE (PUBLIC) =====

    @GetMapping
    @Operation(summary = "Get marketplace apps with optional category filter")
    public ResponseEntity<Page<AppResponse>> getMarketplaceApps(
            @RequestParam(required = false) AppCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "downloadCount") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(appService.getMarketplaceApps(category, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search apps by name, description, or tags")
    public ResponseEntity<Page<AppResponse>> searchApps(
            @RequestParam String query,
            @RequestParam(required = false) AppCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(appService.searchApps(query, category, pageable));
    }

    @GetMapping("/{appId}")
    @Operation(summary = "Get app details by ID")
    public ResponseEntity<AppResponse> getAppById(@PathVariable Long appId) {
        return ResponseEntity.ok(appService.getAppById(appId));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured apps for homepage")
    public ResponseEntity<List<AppResponse>> getFeaturedApps() {
        return ResponseEntity.ok(appService.getFeaturedApps());
    }

    @GetMapping("/trending")
    @Operation(summary = "Get top trending apps by download count")
    public ResponseEntity<List<AppResponse>> getTrendingApps(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(appService.getTrendingApps(limit));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top-rated apps")
    public ResponseEntity<List<AppResponse>> getTopRatedApps(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(appService.getTopRatedApps(limit));
    }

    @GetMapping("/{appId}/similar")
    @Operation(summary = "Get similar apps for recommendations")
    public ResponseEntity<List<AppResponse>> getSimilarApps(
            @PathVariable Long appId,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(appService.getSimilarApps(appId, limit));
    }

    // ===== DEVELOPER CONSOLE (DEVELOPER, ADMIN) =====

    @PostMapping
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Submit a new app for review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AppResponse> createApp(
            @Valid @RequestBody AppRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long developerId = getUserId(userDetails.getUsername());
        log.info("POST /api/apps - developer: {}", developerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appService.createApp(request, developerId));
    }

    @PutMapping("/{appId}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Update an existing app", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AppResponse> updateApp(
            @PathVariable Long appId,
            @Valid @RequestBody AppRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long developerId = getUserId(userDetails.getUsername());
        return ResponseEntity.ok(appService.updateApp(appId, request, developerId));
    }

    @DeleteMapping("/{appId}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Delete an app", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteApp(
            @PathVariable Long appId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long developerId = getUserId(userDetails.getUsername());
        appService.deleteApp(appId, developerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-apps")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Get all apps published by current developer",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AppResponse>> getMyApps(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long developerId = getUserId(userDetails.getUsername());
        return ResponseEntity.ok(appService.getDeveloperApps(developerId));
    }

    @PostMapping("/{appId}/download")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record an app download", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> recordDownload(
            @PathVariable Long appId,
            @RequestParam(required = false, defaultValue = "WEB") String platform,
            @RequestParam(required = false, defaultValue = "Unknown") String country,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails.getUsername());
        appService.recordDownload(appId, userId, platform, country);
        return ResponseEntity.ok().build();
    }

    // ===== ADMIN =====

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all apps pending review (admin only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AppResponse>> getPendingApps() {
        return ResponseEntity.ok(appService.getPendingApps());
    }

    @PatchMapping("/{appId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update app approval status (admin only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AppResponse> updateAppStatus(
            @PathVariable Long appId,
            @RequestParam AppStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long adminId = getUserId(userDetails.getUsername());
        return ResponseEntity.ok(appService.updateAppStatus(appId, status, adminId));
    }

    @PatchMapping("/{appId}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle app featured status (admin only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AppResponse> toggleFeatured(@PathVariable Long appId) {
        return ResponseEntity.ok(appService.toggleFeatured(appId));
    }

    // ===== HELPER =====

    private Long getUserId(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getUserId();
    }
}
