package com.appverse.service.impl;
 
import com.appverse.dto.response.AppResponse;
import com.appverse.entity.*;
import com.appverse.pattern.strategy.RecommendationContext;
import com.appverse.pattern.strategy.TopRatedRecommendationStrategy;
import com.appverse.pattern.strategy.TrendingRecommendationStrategy;
import com.appverse.repository.AppRepository;
import com.appverse.repository.DownloadRepository;
import com.appverse.repository.UserRepository;
import com.appverse.service.AppService;
import com.appverse.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.*;
import java.util.stream.Collectors;
 
/**
 * AI Recommendation Engine Implementation.
 *
 * Design Patterns used:
 * - Strategy Pattern: switches recommendation algorithm
 *   based on user download history
 *   (TrendingStrategy for new users,
 *    TopRatedStrategy for active users)
 *
 * Recommendation approach:
 * - New user (cold start) → Trending apps
 * - Active user           → Top rated + category preference
 * - Download prediction   → Rule-based growth model
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
 
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final DownloadRepository downloadRepository;
    private final AppService appService;
 
    // Strategy Pattern dependencies
    private final RecommendationContext recommendationContext;
    private final TrendingRecommendationStrategy trendingStrategy;
    private final TopRatedRecommendationStrategy topRatedStrategy;
 
    // Category growth coefficients
    private static final Map<AppCategory, Double> CATEGORY_GROWTH =
            Map.of(
                AppCategory.PRODUCTIVITY,    1.3,
                AppCategory.ENTERTAINMENT,   1.2,
                AppCategory.EDUCATION,       1.1,
                AppCategory.GAMING,          1.4,
                AppCategory.FINANCE,         1.0,
                AppCategory.HEALTH_FITNESS,  1.15,
                AppCategory.SOCIAL,          1.25,
                AppCategory.UTILITIES,       0.9
            );
 
    @Override
    @Transactional(readOnly = true)
    public List<AppResponse> getPersonalizedRecommendations(
            Long userId, int limit) {
 
        log.info("Generating recommendations for user: {}", userId);
 
        // Get user download history
        List<Download> userDownloads =
                downloadRepository.findByUser_UserId(userId);
 
        // Strategy Pattern — select algorithm based on user history
        if (userDownloads.isEmpty()) {
            // Cold start: new user has no history → use trending
            recommendationContext.setStrategy(trendingStrategy);
            log.debug("Cold start user {} — TrendingStrategy selected",
                    userId);
        } else {
            // Active user has history → use top rated
            recommendationContext.setStrategy(topRatedStrategy);
            log.debug("Active user {} — TopRatedStrategy selected",
                    userId);
        }
 
        // Execute selected strategy
        List<App> strategyApps =
                recommendationContext.executeStrategy(userId, limit);
 
        // If strategy returned results, map and return them
        if (!strategyApps.isEmpty()) {
            Set<Long> downloadedAppIds = userDownloads.stream()
                    .map(d -> d.getApp().getAppId())
                    .collect(Collectors.toSet());
 
            return strategyApps.stream()
                    .filter(a -> !downloadedAppIds.contains(a.getAppId()))
                    .map(a -> appService.getAppById(a.getAppId()))
                    .limit(limit)
                    .collect(Collectors.toList());
        }
 
        // Fallback: return trending if strategy returns empty
        log.debug("Strategy returned empty, falling back to trending");
        return appService.getTrendingApps(limit);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTrendingAnalysis() {
        log.debug("Generating trending analysis");
 
        List<AppResponse> trending = appService.getTrendingApps(10);
 
        Map<String, Long> trendingByCategory = trending.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCategory().name(),
                        Collectors.counting()
                ));
 
        long totalDownloads = downloadRepository.count();
 
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("trendingApps", trending);
        analysis.put("trendingByCategory", trendingByCategory);
        analysis.put("totalPlatformDownloads", totalDownloads);
        analysis.put("topCategory",
                trendingByCategory.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("N/A"));
        analysis.put("generatedAt", java.time.LocalDateTime.now());
 
        return analysis;
    }
 
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> predictDownloads(Long appId) {
        log.info("Predicting downloads for app: {}", appId);
 
        AppResponse app = appService.getAppById(appId);
 
        long currentDownloads =
                app.getDownloadCount() != null
                        ? app.getDownloadCount() : 0L;
 
        // Rating multiplier: higher rated apps grow faster
        double ratingMultiplier = app.getAverageRating() != null
                ? 0.5 + (app.getAverageRating().doubleValue() / 5.0)
                : 0.8;
 
        // Category growth coefficient
        double categoryGrowth = CATEGORY_GROWTH
                .getOrDefault(app.getCategory(), 1.0);
 
        // Predict 30-day growth (4 weeks × weekly growth rate)
        double weeklyGrowthRate =
                0.10 * ratingMultiplier * categoryGrowth;
 
        long predicted30Day = (long) (currentDownloads
                * Math.pow(1 + weeklyGrowthRate, 4));
 
        // Confidence based on existing download volume
        double confidence;
        if (currentDownloads > 1000)      confidence = 0.85;
        else if (currentDownloads > 100)  confidence = 0.70;
        else                              confidence = 0.50;
 
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("appId", appId);
        prediction.put("appName", app.getName());
        prediction.put("currentDownloads", currentDownloads);
        prediction.put("predicted30DayDownloads", predicted30Day);
        prediction.put("estimatedGrowth",
                predicted30Day - currentDownloads);
        prediction.put("growthRatePercent",
                Math.round(weeklyGrowthRate * 4 * 100));
        prediction.put("confidenceScore", confidence);
        prediction.put("ratingMultiplier",
                Math.round(ratingMultiplier * 100.0) / 100.0);
        prediction.put("categoryGrowthFactor", categoryGrowth);
        prediction.put("modelVersion", "strategy-pattern-v2.0");
        prediction.put("strategyUsed",
                recommendationContext.getCurrentStrategyName());
        prediction.put("generatedAt", java.time.LocalDateTime.now());
 
        return prediction;
    }
 
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCategoryInsights() {
        List<Object[]> categoryCounts =
                appRepository.countAppsByCategory();
 
        Map<String, Long> appCounts = new LinkedHashMap<>();
        for (Object[] row : categoryCounts) {
            appCounts.put(row[0].toString(), (Long) row[1]);
        }
 
        Map<String, Object> insights = new HashMap<>();
        insights.put("appCountByCategory", appCounts);
        insights.put("categoryGrowthCoefficients",
                CATEGORY_GROWTH.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().name(),
                                Map.Entry::getValue
                        )));
        insights.put("fastestGrowingCategory",
                CATEGORY_GROWTH.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(e -> e.getKey().name())
                        .orElse("N/A"));
        insights.put("currentStrategy",
                recommendationContext.getCurrentStrategyName());
        insights.put("generatedAt", java.time.LocalDateTime.now());
 
        return insights;
    }
}
 