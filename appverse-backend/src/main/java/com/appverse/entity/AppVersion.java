package com.appverse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a specific version release of an app.
 * Maintains complete version history for each app in the Developer Console.
 *
 * Relationships:
 * - ManyToOne -> App (the app this version belongs to)
 */
@Entity
@Table(
    name = "app_versions",
    indexes = {
        @Index(name = "idx_version_app", columnList = "app_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Long versionId;

    /**
     * Semantic version string for this release (e.g., "1.2.0").
     */
    @NotBlank(message = "Version number is required")
    @Size(max = 20, message = "Version must not exceed 20 characters")
    @Column(name = "version_number", nullable = false, length = 20)
    private String versionNumber;

    /**
     * Public release notes describing what changed in this version.
     */
    @Size(max = 2000, message = "Release notes must not exceed 2000 characters")
    @Column(name = "release_notes", columnDefinition = "TEXT")
    private String releaseNotes;

    /**
     * URL to the downloadable package for this specific version.
     */
    @Column(name = "download_url")
    private String downloadUrl;

    /**
     * File size in bytes for this version's package.
     */
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    /**
     * Whether this is the currently active/latest version.
     */
    @Column(name = "is_current")
    @Builder.Default
    private Boolean isCurrent = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @CreationTimestamp
    @Column(name = "released_at", nullable = false, updatable = false)
    private LocalDateTime releasedAt;
}
