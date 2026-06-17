package com.appverse.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response format returned by GlobalExceptionHandler.
 *
 * All API errors (validation failures, not-found, auth errors, etc.)
 * are wrapped in this consistent structure so the frontend can
 * handle them uniformly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    /** HTTP status code (e.g., 400, 401, 404, 500). */
    private int status;

    /** Machine-readable error code (e.g., "VALIDATION_FAILED", "RESOURCE_NOT_FOUND"). */
    private String error;

    /** Human-readable error message. */
    private String message;

    /** API path that triggered the error. */
    private String path;

    /** Timestamp when the error occurred. */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Field-level validation errors (only present on 400 validation failures).
     * Key = field name, Value = validation error message.
     */
    private Map<String, String> fieldErrors;
}
