package com.appverse.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for submitting a new review or updating an existing review.
 */
@Data
public class ReviewRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Review text must not exceed 2000 characters")
    private String reviewText;
}
