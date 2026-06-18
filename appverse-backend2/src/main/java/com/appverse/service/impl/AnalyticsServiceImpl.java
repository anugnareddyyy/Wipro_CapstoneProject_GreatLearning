package com.appverse.service.impl;
 
import com.appverse.entity.App;
import com.appverse.entity.Download;
import com.appverse.repository.AppRepository;
import com.appverse.repository.DownloadRepository;
import com.appverse.repository.ReviewRepository;
import com.appverse.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
 
/**
 * Implementation of AnalyticsService.
 *
 * Aggregates data across DownloadRepository, AppRepository,
 * and ReviewRepository to build the Developer Analytics
 * Dashboard (Module 6 of capstone spec):
 * - Downloads analytics (daily trend, by app)
 * - Revenue insights (price × downloads per app)
 * - User engagement reports (reviews, ratings, retention signal)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {
 
    private final AppRepository appRepository;
    private final DownloadRepository downloadRepository;
    private final ReviewRepository reviewRepository;
 
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDeveloperAnalytics(Long developerId) {
        log.info("Building analytics dashboard for developer: {}", developerId);
 
        List<App> myApps = appRepository.findByDeveloper_UserId(developerId);
 
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("downloadsAnalytics", buildDownloadsAnalytics(myApps));
        dashboard.put("revenueInsights", buildRevenueInsights(myApps));
        dashboard.put("engagementReports", buildEngagementReports(myApps));
        dashboard.put("generatedAt", LocalDateTime.now());
 
        return dashboard;
    }
 
    /**
     * Build downloads analytics: total downloads per app,
     * and a 7-day download trend across all apps combined.
     */
    private Map<String, Object> buildDownloadsAnalytics(List<App> apps) {
        Map<String, Object> result = new HashMap<>();
 
        // Total downloads per app (for bar chart)
        List<Map<String, Object>> perApp = apps.stream()
                .map(app -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("appName", app.getName());
                    row.put("downloads", app.getDownloadCount() != null
                            ? app.getDownloadCount() : 0L);
                    return row;
                })
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("downloads"), (Long) a.get("downloads")))
                .collect(Collectors.toList());
 
        // 7-day trend combined across all apps (for line chart)
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        Map<String, Long> dailyTrend = new TreeMap<>();
 
        for (App app : apps) {
            List<Object[]> dayData =
                    downloadRepository.getDownloadsByDay(app.getAppId(), since);
            for (Object[] row : dayData) {
                String day = row[0].toString();
                long count = (Long) row[1];
                dailyTrend.merge(day, count, Long::sum);
            }
        }
 
        long totalDownloads = apps.stream()
                .mapToLong(a -> a.getDownloadCount() != null ? a.getDownloadCount() : 0L)
                .sum();
 
        result.put("perApp", perApp);
        result.put("dailyTrend", dailyTrend);
        result.put("totalDownloads", totalDownloads);
 
        return result;
    }
 
    /**
     * Build revenue insights: revenue per app (price × downloads),
     * total revenue, and revenue distribution by category.
     */
    private Map<String, Object> buildRevenueInsights(List<App> apps) {
        Map<String, Object> result = new HashMap<>();
 
        List<Map<String, Object>> perAppRevenue = apps.stream()
                .map(app -> {
                    BigDecimal price = app.getPrice() != null
                            ? app.getPrice() : BigDecimal.ZERO;
                    long downloads = app.getDownloadCount() != null
                            ? app.getDownloadCount() : 0L;
                    BigDecimal revenue = price.multiply(BigDecimal.valueOf(downloads));
 
                    Map<String, Object> row = new HashMap<>();
                    row.put("appName", app.getName());
                    row.put("price", price);
                    row.put("downloads", downloads);
                    row.put("revenue", revenue);
                    return row;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("revenue"))
                        .compareTo((BigDecimal) a.get("revenue")))
                .collect(Collectors.toList());
 
        BigDecimal totalRevenue = perAppRevenue.stream()
                .map(row -> (BigDecimal) row.get("revenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
 
        // Free vs Paid app split
        long freeApps = apps.stream()
                .filter(a -> a.getPrice() == null
                        || a.getPrice().compareTo(BigDecimal.ZERO) == 0)
                .count();
        long paidApps = apps.size() - freeApps;
 
        result.put("perApp", perAppRevenue);
        result.put("totalRevenue", totalRevenue);
        result.put("freeAppsCount", freeApps);
        result.put("paidAppsCount", paidApps);
 
        return result;
    }
 
    /**
     * Build user engagement reports: review counts, average ratings,
     * and a simple engagement score per app.
     */
    private Map<String, Object> buildEngagementReports(List<App> apps) {
        Map<String, Object> result = new HashMap<>();
 
        List<Map<String, Object>> perAppEngagement = apps.stream()
                .map(app -> {
                    long reviewCount = reviewRepository
                            .countByApp_AppIdAndIsVisibleTrue(app.getAppId());
                    double avgRating = app.getAverageRating() != null
                            ? app.getAverageRating().doubleValue() : 0.0;
                    long downloads = app.getDownloadCount() != null
                            ? app.getDownloadCount() : 0L;
 
                    // Simple engagement score: (reviews / downloads) * rating
                    double engagementScore = downloads > 0
                            ? Math.round(((double) reviewCount / downloads) * avgRating * 100) / 100.0
                            : 0.0;
 
                    Map<String, Object> row = new HashMap<>();
                    row.put("appName", app.getName());
                    row.put("reviewCount", reviewCount);
                    row.put("averageRating", avgRating);
                    row.put("engagementScore", engagementScore);
                    return row;
                })
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("engagementScore"),
                        (Double) a.get("engagementScore")))
                .collect(Collectors.toList());
 
        long totalReviews = perAppEngagement.stream()
                .mapToLong(row -> (Long) row.get("reviewCount"))
                .sum();
 
        double avgRatingAcrossApps = apps.stream()
                .filter(a -> a.getAverageRating() != null)
                .mapToDouble(a -> a.getAverageRating().doubleValue())
                .average()
                .orElse(0.0);
 
        result.put("perApp", perAppEngagement);
        result.put("totalReviews", totalReviews);
        result.put("averageRatingAcrossApps",
                Math.round(avgRatingAcrossApps * 100) / 100.0);
 
        return result;
    }
}
 