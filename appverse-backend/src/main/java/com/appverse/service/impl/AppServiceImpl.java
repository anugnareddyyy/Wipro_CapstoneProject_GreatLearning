package com.appverse.service.impl;
 
import com.appverse.dto.request.AppRequest;
import com.appverse.dto.response.AppResponse;
import com.appverse.entity.*;
import com.appverse.exception.ResourceNotFoundException;
import com.appverse.exception.UnauthorizedActionException;
import com.appverse.pattern.observer.AppStatusEventPublisher;
import com.appverse.repository.AppRepository;
import com.appverse.repository.DownloadRepository;
import com.appverse.repository.UserRepository;
import com.appverse.service.AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
 
/**
* Implementation of AppService handling all marketplace
* and developer console logic.
*
* Design Patterns used:
* - Observer Pattern: notifies observers on app status change
* - Builder Pattern: AppResponse built via Lombok @Builder
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class AppServiceImpl implements AppService {
 
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final DownloadRepository downloadRepository;
    private final AppStatusEventPublisher statusEventPublisher;
 
    // ===== MARKETPLACE =====
 
    @Override
    @Transactional(readOnly = true)
    public Page<AppResponse> getMarketplaceApps(
            AppCategory category, Pageable pageable) {
 
        log.debug("Fetching marketplace apps - category: {}, page: {}",
                category, pageable.getPageNumber());
 
        Page<App> apps = (category != null)
                ? appRepository.findByStatusAndCategory(
                        AppStatus.APPROVED, category, pageable)
                : appRepository.findByStatus(AppStatus.APPROVED, pageable);
 
        return apps.map(this::mapToResponse);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<AppResponse> searchApps(
            String query, AppCategory category, Pageable pageable) {
 
        log.debug("Searching apps with query: '{}', category: {}",
                query, category);
 
        Page<App> apps = (category != null)
                ? appRepository.searchAppsInCategory(query, category, pageable)
                : appRepository.searchApps(query, pageable);
 
        return apps.map(this::mapToResponse);
    }
 
    @Override
    @Transactional(readOnly = true)
    public AppResponse getAppById(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "App", "appId", appId));
        return mapToResponse(app);
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<AppResponse> getFeaturedApps() {
        return appRepository
                .findByIsFeaturedTrueAndStatus(AppStatus.APPROVED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<AppResponse> getTrendingApps(int limit) {
        return appRepository
                .findTopTrendingApps(PageRequest.of(0, limit))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<AppResponse> getTopRatedApps(int limit) {
        return appRepository
                .findTopRatedApps(PageRequest.of(0, limit))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<AppResponse> getSimilarApps(Long appId, int limit) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "App", "appId", appId));
 
        return appRepository
                .findSimilarApps(
                        app.getCategory(), appId, PageRequest.of(0, limit))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    // ===== DEVELOPER CONSOLE =====
 
    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public AppResponse createApp(AppRequest request, Long developerId) {
        log.info("Developer {} creating new app: {}",
                developerId, request.getName());
 
        User developer = userRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "userId", developerId));
 
        App app = App.builder()
                .name(request.getName())
                .description(request.getDescription())
                .tagline(request.getTagline())
                .category(request.getCategory())
                .price(request.getPrice())
                .iconUrl(request.getIconUrl())
                .tags(request.getTags())
                .currentVersion(request.getCurrentVersion() != null
                        ? request.getCurrentVersion() : "1.0.0")
                .status(AppStatus.PENDING)
                .developer(developer)
                .build();
 
        App savedApp = appRepository.save(app);
        log.info("App created with id: {} and status PENDING",
                savedApp.getAppId());
 
        return mapToResponse(savedApp);
    }
 
    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public AppResponse updateApp(
            Long appId, AppRequest request, Long developerId) {
 
        log.info("Developer {} updating app: {}", developerId, appId);
 
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "App", "appId", appId));
 
        if (!app.getDeveloper().getUserId().equals(developerId)) {
            throw new UnauthorizedActionException(
                    "You are not the owner of this app");
        }
 
        app.setName(request.getName());
        app.setDescription(request.getDescription());
        app.setTagline(request.getTagline());
        app.setCategory(request.getCategory());
        app.setPrice(request.getPrice());
        app.setIconUrl(request.getIconUrl());
        app.setTags(request.getTags());
 
        if (request.getCurrentVersion() != null) {
            app.setCurrentVersion(request.getCurrentVersion());
        }
 
        return mapToResponse(appRepository.save(app));
    }
 
    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public void deleteApp(Long appId, Long developerId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "App", "appId", appId));
 
        if (!app.getDeveloper().getUserId().equals(developerId)) {
            throw new UnauthorizedActionException(
                    "You are not the owner of this app");
        }
 
        appRepository.delete(app);
        log.info("App {} deleted by developer {}", appId, developerId);
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<AppResponse> getDeveloperApps(Long developerId) {
        return appRepository.findByDeveloper_UserId(developerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional
    public void recordDownload(
            Long appId, Long userId,
            String platform, String country) {
 
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "App", "appId", appId));
 
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "userId", userId));
 
        Download download = Download.builder()
                .app(app)
                .user(user)
                .platform(platform)
                .country(country)
                .versionDownloaded(app.getCurrentVersion())
                .build();
 
        downloadRepository.save(download);
        appRepository.incrementDownloadCount(appId);
 
        log.info("Download recorded: appId={}, userId={}, platform={}",
                appId, userId, platform);
    }
 
    // ===== ADMIN =====
 
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppResponse> getPendingApps() {
        return appRepository
                .findByStatusOrderByCreatedAtAsc(AppStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public AppResponse updateAppStatus(
            Long appId, AppStatus status, Long adminId) {
 
        log.info("Admin {} updating app {} status to {}",
                adminId, appId, status);
 
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "App", "appId", appId));
 
        // Save old status for Observer Pattern notification
        AppStatus oldStatus = app.getStatus();
 
        app.setStatus(status);
        App savedApp = appRepository.save(app);
 
        // Observer Pattern — notify all registered observers
        // (DeveloperNotificationObserver, AuditLogObserver)
        statusEventPublisher.notifyObservers(savedApp, oldStatus, status);
 
        log.info("App {} status changed from {} to {}",
                appId, oldStatus, status);
 
        return mapToResponse(savedApp);
    }
 
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public AppResponse toggleFeatured(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "App", "appId", appId));
 
        app.setIsFeatured(!app.getIsFeatured());
        return mapToResponse(appRepository.save(app));
    }
 
    // ===== MAPPING =====
 
    /**
     * Map App entity to AppResponse DTO.
     * Flattens developer info, excludes lazy collections.
     *
     * @param app the App entity to map
     * @return AppResponse DTO
     */
    private AppResponse mapToResponse(App app) {
        return AppResponse.builder()
                .appId(app.getAppId())
                .name(app.getName())
                .description(app.getDescription())
                .tagline(app.getTagline())
                .category(app.getCategory())
                .price(app.getPrice())
                .iconUrl(app.getIconUrl())
                .downloadUrl(app.getDownloadUrl())
                .currentVersion(app.getCurrentVersion())
                .averageRating(app.getAverageRating())
                .reviewCount(app.getReviewCount())
                .downloadCount(app.getDownloadCount())
                .status(app.getStatus())
                .isFeatured(app.getIsFeatured())
                .tags(app.getTags())
                .developerId(app.getDeveloper().getUserId())
                .developerName(app.getDeveloper().getUsername())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}