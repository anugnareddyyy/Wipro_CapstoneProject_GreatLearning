package com.appverse.repository;

import com.appverse.entity.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AppVersion entity supporting version history and release management.
 */
@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {

    /**
     * Get all versions for an app ordered by release date (newest first).
     */
    List<AppVersion> findByApp_AppIdOrderByReleasedAtDesc(Long appId);

    /**
     * Get the current active version for an app.
     */
    Optional<AppVersion> findByApp_AppIdAndIsCurrentTrue(Long appId);

    /**
     * Mark all versions of an app as non-current before setting a new current version.
     */
    @Modifying
    @Query("UPDATE AppVersion av SET av.isCurrent = false WHERE av.app.appId = :appId")
    void clearCurrentVersionForApp(@Param("appId") Long appId);

    /**
     * Check if a version number already exists for an app (prevents duplicate versions).
     */
    boolean existsByApp_AppIdAndVersionNumber(Long appId, String versionNumber);
}
