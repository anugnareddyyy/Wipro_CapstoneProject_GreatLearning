package com.appverse.service;
 
import java.util.Map;
 
/**
* Service interface for developer analytics dashboard.
* Provides downloads, revenue, and engagement insights
* as required by capstone Module 6.
*/
public interface AnalyticsService {
 
    /**
     * Get complete analytics dashboard data for a developer.
     * Includes downloads over time, revenue breakdown,
     * and user engagement metrics across all their apps.
     *
     * @param developerId the developer's user ID
     * @return map containing all dashboard sections
     */
    Map<String, Object> getDeveloperAnalytics(Long developerId);
}