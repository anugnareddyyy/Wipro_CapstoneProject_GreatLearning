package com.appverse.dto.request;

import com.appverse.entity.AppCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for creating or updating an app via the Developer Console.
 * Decouples the REST API surface from the internal App entity.
 */
@Data
public class AppRequest {

    @NotBlank(message = "App name is required")
    @Size(min = 2, max = 100, message = "App name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "App description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description;

    @Size(max = 200, message = "Tagline must not exceed 200 characters")
    private String tagline;

    @NotNull(message = "Category is required")
    private AppCategory category;

    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    private BigDecimal price;

    private String iconUrl;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    @Size(max = 20, message = "Version must not exceed 20 characters")
    private String currentVersion;

    private String releaseNotes;
}
