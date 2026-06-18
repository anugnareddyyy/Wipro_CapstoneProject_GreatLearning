package com.appverse.entity;

/**
 * Lifecycle status of an application on the AppVerse marketplace.
 *
 * PENDING  -> Submitted by developer, awaiting admin review
 * APPROVED -> Reviewed and published on the marketplace
 * REJECTED -> Rejected by admin with a reason
 * SUSPENDED -> Temporarily removed from the marketplace
 */
public enum AppStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED
}
