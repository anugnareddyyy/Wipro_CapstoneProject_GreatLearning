package com.appverse.service;

import com.appverse.dto.request.AppRequest;
import com.appverse.dto.response.AppResponse;
import com.appverse.entity.AppCategory;
import com.appverse.entity.AppStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for App marketplace operations.
 * Covers CRUD for apps, marketplace browsing, developer console, and admin management.
 */
public interface AppService {

    // ===== MARKETPLACE (public) =====

    /** Get paginated list of approved apps, optionally filtered by category. */
    Page<AppResponse> getMarketplaceApps(AppCategory category, Pageable pageable);

    /** Full-text search across name, description, and tags. */
    Page<AppResponse> searchApps(String query, AppCategory category, Pageable pageable);

    /** Get detailed info for a single approved app. */
    AppResponse getAppById(Long appId);

    /** Get featured apps for homepage carousel. */
    List<AppResponse> getFeaturedApps();

    /** Get top trending apps by download count. */
    List<AppResponse> getTrendingApps(int limit);

    /** Get top-rated apps. */
    List<AppResponse> getTopRatedApps(int limit);

    /** Get similar apps in same category (AI recommendation support). */
    List<AppResponse> getSimilarApps(Long appId, int limit);

    // ===== DEVELOPER CONSOLE =====

    /** Submit a new app for marketplace review. */
    AppResponse createApp(AppRequest request, Long developerId);

    /** Update an existing app (developer only, own apps). */
    AppResponse updateApp(Long appId, AppRequest request, Long developerId);

    /** Delete an app (developer only, own apps). */
    void deleteApp(Long appId, Long developerId);

    /** Get all apps published by a specific developer. */
    List<AppResponse> getDeveloperApps(Long developerId);

    /** Record a download event and increment counter. */
    void recordDownload(Long appId, Long userId, String platform, String country);

    // ===== ADMIN =====

    /** Get apps pending admin approval. */
    List<AppResponse> getPendingApps();

    /** Approve or reject an app (admin only). */
    AppResponse updateAppStatus(Long appId, AppStatus status, Long adminId);

    /** Toggle featured status of an app. */
    AppResponse toggleFeatured(Long appId);
}
