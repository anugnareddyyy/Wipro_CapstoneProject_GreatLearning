package com.appverse.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity tracking each app download event.
 * Used for analytics: download counts, trends, revenue insights.
 *
 * Relationships:
 * - ManyToOne -> User (who downloaded)
 * - ManyToOne -> App (what was downloaded)
 */
@Entity
@Table(
    name = "downloads",
    indexes = {
        @Index(name = "idx_download_app", columnList = "app_id"),
        @Index(name = "idx_download_user", columnList = "user_id"),
        @Index(name = "idx_download_date", columnList = "downloaded_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Download {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "download_id")
    private Long downloadId;

    /**
     * Version of the app that was downloaded.
     */
    @Column(name = "version_downloaded", length = 20)
    private String versionDownloaded;

    /**
     * Country/region where download originated (for geo-analytics).
     */
    @Column(name = "country", length = 50)
    private String country;

    /**
     * Device platform: ANDROID, IOS, WEB, WINDOWS, MAC.
     */
    @Column(name = "platform", length = 20)
    private String platform;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    /**
     * Timestamp of the download event. Set automatically at creation.
     */
    @CreationTimestamp
    @Column(name = "downloaded_at", nullable = false, updatable = false)
    private LocalDateTime downloadedAt;
}
