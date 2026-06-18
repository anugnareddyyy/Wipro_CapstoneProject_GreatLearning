package com.appverse.repository;

import com.appverse.entity.Download;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Download entity supporting analytics queries.
 */
@Repository
public interface DownloadRepository extends JpaRepository<Download, Long> {

    /**
     * Get all downloads for a specific app (used in developer analytics).
     */
    List<Download> findByApp_AppId(Long appId);

    /**
     * Get all downloads by a specific user.
     */
    List<Download> findByUser_UserId(Long userId);

    /**
     * Total download count for an app within a date range.
     */
    @Query("SELECT COUNT(d) FROM Download d WHERE d.app.appId = :appId " +
           "AND d.downloadedAt BETWEEN :start AND :end")
    long countByAppAndDateRange(@Param("appId") Long appId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    /**
     * Downloads per day for chart visualization in developer dashboard.
     */
    @Query("SELECT DATE(d.downloadedAt), COUNT(d) FROM Download d " +
           "WHERE d.app.appId = :appId AND d.downloadedAt >= :since " +
           "GROUP BY DATE(d.downloadedAt) ORDER BY DATE(d.downloadedAt)")
    List<Object[]> getDownloadsByDay(@Param("appId") Long appId,
                                      @Param("since") LocalDateTime since);

    /**
     * Downloads grouped by country for geo-distribution analytics.
     */
    @Query("SELECT d.country, COUNT(d) FROM Download d WHERE d.app.appId = :appId " +
           "AND d.country IS NOT NULL GROUP BY d.country ORDER BY COUNT(d) DESC")
    List<Object[]> getDownloadsByCountry(@Param("appId") Long appId);

    /**
     * Downloads grouped by platform (Android, iOS, Web, etc.).
     */
    @Query("SELECT d.platform, COUNT(d) FROM Download d WHERE d.app.appId = :appId " +
           "GROUP BY d.platform")
    List<Object[]> getDownloadsByPlatform(@Param("appId") Long appId);

    /**
     * Total platform-wide downloads (admin dashboard).
     */
    long count();
}
