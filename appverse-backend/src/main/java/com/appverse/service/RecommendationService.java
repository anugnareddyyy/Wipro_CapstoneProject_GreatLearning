package com.appverse.service;

import com.appverse.dto.response.AppResponse;

import java.util.List;
import java.util.Map;

/**
 * Service interface for AI-powered recommendation and analytics features.
 * Implements the AI Recommendation Engine module from the capstone spec.
 */
public interface RecommendationService {

    /**
     * Get personalized app recommendations for a user.
     * Based on their download history, categories browsed, and similar users.
     *
     * @param userId the user to recommend for
     * @param limit  max number of recommendations to return
     * @return ordered list of recommended apps
     */
    List<AppResponse> getPersonalizedRecommendations(Long userId, int limit);

    /**
     * Get trending apps analysis with category breakdown.
     * Used for homepage trending section and analytics dashboard.
     */
    Map<String, Object> getTrendingAnalysis();

    /**
     * Predict how many downloads an app is likely to get in the next 30 days.
     * Based on current download velocity, rating, and category trends.
     *
     * @param appId the app to predict downloads for
     * @return prediction map with estimate and confidence
     */
    Map<String, Object> predictDownloads(Long appId);

    /**
     * Get category-level recommendation insights.
     * Shows which categories are growing and likely to be popular.
     */
    Map<String, Object> getCategoryInsights();
}
