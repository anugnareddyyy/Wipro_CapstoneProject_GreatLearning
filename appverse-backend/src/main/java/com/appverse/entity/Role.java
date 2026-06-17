package com.appverse.entity;

/**
 * Enumeration representing user roles in the AppVerse platform.
 * Used for role-based access control (RBAC) across the application.
 *
 * - USER: Standard marketplace user who can browse, review, and download apps
 * - DEVELOPER: Can publish and manage apps through the Developer Console
 * - ADMIN: Full platform access including moderation and user management
 */
public enum Role {
    USER,
    DEVELOPER,
    ADMIN
}
