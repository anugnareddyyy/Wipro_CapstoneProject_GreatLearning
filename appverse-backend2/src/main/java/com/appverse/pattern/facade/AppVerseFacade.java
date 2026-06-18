package com.appverse.pattern.facade;
 
import com.appverse.dto.response.AppResponse;
import com.appverse.dto.response.ReviewResponse;
import com.appverse.service.AppService;
import com.appverse.service.RecommendationService;
import com.appverse.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
/**
* Facade Pattern — Provides a simplified interface to the
* complex subsystems of AppVerse.
*
* Instead of the frontend/controller calling multiple services,
* the Facade provides single-method calls that coordinate
* multiple services internally.
*
* Benefits:
* - Reduces coupling between controllers and services
* - Simplifies complex multi-service operations
* - Single entry point for dashboard data loading
*/
@Component
@RequiredArgsConstructor
@Slf4j
public class AppVerseFacade {
 
    private final AppService appService;
    private final ReviewService reviewService;
    private final RecommendationService recommendationService;
 
    /**
     * Load all data needed for the home page in one call.
     * Internally coordinates AppService and RecommendationService.
     *
     * @return map containing featured, trending, and top-rated apps
     */
    public Map<String, Object> getHomePageData() {
        log.info("Facade: Loading homepage data");
 
        Map<String, Object> data = new HashMap<>();
        data.put("featuredApps", appService.getFeaturedApps());
        data.put("trendingApps", appService.getTrendingApps(8));
        data.put("topRatedApps", appService.getTopRatedApps(8));
        data.put("trendingAnalysis", recommendationService.getTrendingAnalysis());
 
        log.info("Facade: Homepage data loaded successfully");
        return data;
    }
 
    /**
     * Load complete app detail page data in one call.
     * Combines app info, reviews, similar apps, and sentiment.
     *
     * @param appId the app to load
     * @return map containing all app detail data
     */
    public Map<String, Object> getAppDetailData(Long appId) {
        log.info("Facade: Loading app detail data for appId: {}", appId);
 
        Map<String, Object> data = new HashMap<>();
        data.put("app", appService.getAppById(appId));
        data.put("similarApps", appService.getSimilarApps(appId, 4));
        data.put("sentimentAnalysis", reviewService.getSentimentAnalysis(appId));
 
        log.info("Facade: App detail data loaded for appId: {}", appId);
        return data;
    }
 
    /**
     * Load developer console dashboard data in one call.
     * Combines app list and download predictions.
     *
     * @param developerId the developer's user ID
     * @return map with developer apps and analytics
     */
    public Map<String, Object> getDeveloperDashboardData(Long developerId) {
        log.info("Facade: Loading developer dashboard for developerId: {}", developerId);
 
        List<AppResponse> myApps = appService.getDeveloperApps(developerId);
 
        Map<String, Object> data = new HashMap<>();
        data.put("myApps", myApps);
        data.put("totalApps", myApps.size());
        data.put("liveApps",
            myApps.stream()
                .filter(a -> "APPROVED".equals(a.getStatus().name()))
                .count());
        data.put("totalDownloads",
            myApps.stream()
                .mapToLong(a -> a.getDownloadCount() != null ? a.getDownloadCount() : 0)
                .sum());
 
        log.info("Facade: Developer dashboard loaded");
        return data;
    }
}