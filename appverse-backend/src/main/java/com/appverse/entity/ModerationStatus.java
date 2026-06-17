package com.appverse.entity;
 
/**
* Represents the moderation state of a review.
* Used by admins to track review approval workflow.
*/
public enum ModerationStatus {
    PENDING,
    APPROVED,
    REMOVED
}