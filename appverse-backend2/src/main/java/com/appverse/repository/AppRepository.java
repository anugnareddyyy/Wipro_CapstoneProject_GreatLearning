package com.appverse.repository;

import com.appverse.entity.App;
import com.appverse.entity.AppCategory;
import com.appverse.entity.AppStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for App entity with advanced search, filtering, and analytics queries.
 */
@Repository
public interface AppRepository extends JpaRepository<App, Long> {

    /**
     * Get all approved apps with optional pagination (marketplace listing).
     */
    Page<App> findByStatus(AppStatus status, Pageable pageable);

    /**
     * Filter approved apps by category.
     */
    Page<App> findByStatusAndCategory(AppStatus status, AppCategory category, Pageable pageable);

    /**
     * Full-text search across app name, description, and tags.
     */
    @Query("SELECT a FROM App a WHERE a.status = 'APPROVED' AND (" +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.tags) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<App> searchApps(@Param("query") String query, Pageable pageable);

    /**
     * Search within a specific category.
     */
    @Query("SELECT a FROM App a WHERE a.status = 'APPROVED' AND a.category = :category AND (" +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<App> searchAppsInCategory(@Param("query") String query,
                                    @Param("category") AppCategory category,
                                    Pageable pageable);

    /**
     * Get all apps published by a specific developer (Developer Console).
     */
    List<App> findByDeveloper_UserId(Long developerId);

    /**
     * Get featured apps for homepage highlights.
     */
    List<App> findByIsFeaturedTrueAndStatus(AppStatus status);

    /**
     * Get top trending apps by download count.
     */
    @Query("SELECT a FROM App a WHERE a.status = 'APPROVED' ORDER BY a.downloadCount DESC")
    List<App> findTopTrendingApps(Pageable pageable);

    /**
     * Get top-rated apps by average rating.
     */
    @Query("SELECT a FROM App a WHERE a.status = 'APPROVED' AND a.reviewCount >= 5 " +
           "ORDER BY a.averageRating DESC")
    List<App> findTopRatedApps(Pageable pageable);

    /**
     * Get apps in the same category (used for similar app recommendations).
     */
    @Query("SELECT a FROM App a WHERE a.status = 'APPROVED' AND a.category = :category " +
           "AND a.appId != :excludeId ORDER BY a.downloadCount DESC")
    List<App> findSimilarApps(@Param("category") AppCategory category,
                               @Param("excludeId") Long excludeId,
                               Pageable pageable);

    /**
     * Increment download count atomically (avoids stale read issues).
     */
    @Modifying
    @Query("UPDATE App a SET a.downloadCount = a.downloadCount + 1 WHERE a.appId = :appId")
    void incrementDownloadCount(@Param("appId") Long appId);

    /**
     * Count total apps per category for analytics dashboard.
     */
    @Query("SELECT a.category, COUNT(a) FROM App a WHERE a.status = 'APPROVED' GROUP BY a.category")
    List<Object[]> countAppsByCategory();

    /**
     * Total number of approved apps in marketplace.
     */
    long countByStatus(AppStatus status);

    /**
     * Find apps pending admin review.
     */
    List<App> findByStatusOrderByCreatedAtAsc(AppStatus status);
}
