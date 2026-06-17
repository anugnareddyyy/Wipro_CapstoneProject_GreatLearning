package com.appverse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a published application on the AppVerse marketplace.
 *
 * Relationships:
 * - ManyToOne -> User (developer who published the app)
 * - OneToMany -> Review (user reviews for this app)
 * - OneToMany -> Download (download records)
 * - OneToMany -> AppVersion (version history)
 *
 * Business Rules:
 * - Apps start in PENDING status and must be approved by Admin
 * - Price of 0.00 represents free apps
 * - averageRating is recalculated whenever a new review is added
 */
@Entity
@Table(
    name = "apps",
    indexes = {
        @Index(name = "idx_app_category", columnList = "category"),
        @Index(name = "idx_app_status", columnList = "status"),
        @Index(name = "idx_app_developer", columnList = "developer_id"),
        @Index(name = "idx_app_name", columnList = "name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class App {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_id")
    private Long appId;

    /**
     * Name of the application. Must be unique per developer.
     */
    @NotBlank(message = "App name is required")
    @Size(min = 2, max = 100, message = "App name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Detailed description of the app's features and purpose.
     */
    @NotBlank(message = "App description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Short one-line tagline for listing cards.
     */
    @Size(max = 200, message = "Tagline must not exceed 200 characters")
    @Column(name = "tagline", length = 200)
    private String tagline;

    /**
     * Marketplace category used for filtering and recommendations.
     */
    @NotNull(message = "App category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private AppCategory category;

    /**
     * Price in USD. Zero means free.
     */
    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    @Column(name = "price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * URL to the app icon image (stored on file system or CDN).
     */
    @Column(name = "icon_url")
    private String iconUrl;

    /**
     * URL to the downloadable app package.
     */
    @Column(name = "download_url")
    private String downloadUrl;

    /**
     * Current version string (e.g., "2.1.3").
     */
    @Size(max = 20, message = "Version must not exceed 20 characters")
    @Column(name = "current_version", length = 20)
    private String currentVersion;

    /**
     * Cumulative average rating (0.0 - 5.0), recalculated on each review.
     */
    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    /**
     * Total number of reviews submitted for this app.
     */
    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    /**
     * Total number of times this app has been downloaded.
     */
    @Column(name = "download_count")
    @Builder.Default
    private Long downloadCount = 0L;

    /**
     * Current review/approval status of the app.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AppStatus status = AppStatus.PENDING;

    /**
     * Whether the app is featured on the homepage.
     */
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    /**
     * Tags for better search and AI recommendation matching.
     * Stored as comma-separated values.
     */
    @Column(name = "tags", length = 500)
    private String tags;

    /**
     * The developer (User with DEVELOPER role) who published this app.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = false)
    private User developer;

    /**
     * All user reviews for this app.
     */
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    /**
     * Download records for tracking and analytics.
     */
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Download> downloads = new ArrayList<>();

    /**
     * Version history of the app.
     */
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AppVersion> versions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
