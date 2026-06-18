package com.appverse.dto.response;

import com.appverse.entity.AppCategory;
import com.appverse.entity.AppStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO returned when listing or viewing app details.
 * Shields internal entity fields and computed developer info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppResponse {

    private Long appId;
    private String name;
    private String description;
    private String tagline;
    private AppCategory category;
    private BigDecimal price;
    private String iconUrl;
    private String downloadUrl;
    private String currentVersion;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private Long downloadCount;
    private AppStatus status;
    private Boolean isFeatured;
    private String tags;

    // Developer info (flattened, no nested User object exposed)
    private Long developerId;
    private String developerName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Whether the requesting user has already downloaded this app.
     * Set dynamically in the service layer based on request context.
     */
    private Boolean alreadyDownloaded;

    /**
     * Whether the requesting user has already reviewed this app.
     */
    private Boolean alreadyReviewed;
}
