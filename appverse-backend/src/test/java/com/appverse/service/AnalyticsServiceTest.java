package com.appverse.service;
 
import com.appverse.entity.App;
import com.appverse.entity.AppCategory;
import com.appverse.entity.AppStatus;
import com.appverse.entity.Role;
import com.appverse.entity.User;
import com.appverse.repository.AppRepository;
import com.appverse.repository.DownloadRepository;
import com.appverse.repository.ReviewRepository;
import com.appverse.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
 
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
 
@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Unit Tests")
class AnalyticsServiceTest {
 
    @Mock private AppRepository appRepository;
    @Mock private DownloadRepository downloadRepository;
    @Mock private ReviewRepository reviewRepository;
 
    @InjectMocks
    private AnalyticsServiceImpl analyticsService;
 
    private User developer;
    private App paidApp;
    private App freeApp;
 
    @BeforeEach
    void setUp() {
        developer = User.builder()
                .userId(1L)
                .username("devuser")
                .email("dev@test.com")
                .role(Role.DEVELOPER)
                .isActive(true)
                .build();
 
        paidApp = App.builder()
                .appId(10L)
                .name("Paid App")
                .description("A paid application used for analytics testing")
                .category(AppCategory.PRODUCTIVITY)
                .price(BigDecimal.valueOf(4.99))
                .status(AppStatus.APPROVED)
                .averageRating(BigDecimal.valueOf(4.5))
                .downloadCount(1000L)
                .developer(developer)
                .build();
 
        freeApp = App.builder()
                .appId(11L)
                .name("Free App")
                .description("A free application used for analytics testing")
                .category(AppCategory.GAMING)
                .price(BigDecimal.ZERO)
                .status(AppStatus.APPROVED)
                .averageRating(BigDecimal.valueOf(3.0))
                .downloadCount(500L)
                .developer(developer)
                .build();
    }
 
    @Nested
    @DisplayName("getDeveloperAnalytics()")
    class GetDeveloperAnalytics {
 
        @Test
        @DisplayName("should build a dashboard with all three sections for a developer's apps")
        void getDeveloperAnalytics_ReturnsAllSections() {
            when(appRepository.findByDeveloper_UserId(1L)).thenReturn(List.of(paidApp, freeApp));
            when(downloadRepository.getDownloadsByDay(eq(10L), any(LocalDateTime.class)))
                    .thenReturn(List.of());
            when(downloadRepository.getDownloadsByDay(eq(11L), any(LocalDateTime.class)))
                    .thenReturn(List.of());
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(10L)).thenReturn(50L);
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(11L)).thenReturn(10L);
 
            Map<String, Object> dashboard = analyticsService.getDeveloperAnalytics(1L);
 
            assertThat(dashboard).containsKeys(
                    "downloadsAnalytics", "revenueInsights", "engagementReports", "generatedAt");
        }
 
        @Test
        @DisplayName("should return empty-but-valid dashboard when developer has no apps")
        void getDeveloperAnalytics_WhenNoApps_ReturnsEmptySections() {
            when(appRepository.findByDeveloper_UserId(2L)).thenReturn(List.of());
 
            Map<String, Object> dashboard = analyticsService.getDeveloperAnalytics(2L);
 
            @SuppressWarnings("unchecked")
            Map<String, Object> downloads = (Map<String, Object>) dashboard.get("downloadsAnalytics");
            assertThat(downloads.get("totalDownloads")).isEqualTo(0L);
 
            @SuppressWarnings("unchecked")
            Map<String, Object> revenue = (Map<String, Object>) dashboard.get("revenueInsights");
            assertThat(revenue.get("totalRevenue")).isEqualTo(BigDecimal.ZERO);
            assertThat(revenue.get("freeAppsCount")).isEqualTo(0L);
            assertThat(revenue.get("paidAppsCount")).isEqualTo(0L);
        }
    }
 
    @Nested
    @DisplayName("downloads analytics (via getDeveloperAnalytics)")
    class DownloadsAnalytics {
 
        @Test
        @DisplayName("should sum total downloads across all of the developer's apps")
        void downloadsAnalytics_SumsTotalDownloads() {
            when(appRepository.findByDeveloper_UserId(1L)).thenReturn(List.of(paidApp, freeApp));
            when(downloadRepository.getDownloadsByDay(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(List.of());
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(anyLong())).thenReturn(0L);
 
            Map<String, Object> dashboard = analyticsService.getDeveloperAnalytics(1L);
 
            @SuppressWarnings("unchecked")
            Map<String, Object> downloads = (Map<String, Object>) dashboard.get("downloadsAnalytics");
            assertThat(downloads.get("totalDownloads")).isEqualTo(1500L);
        }
 
        @Test
        @DisplayName("should sort per-app downloads descending")
        void downloadsAnalytics_SortsPerAppDescending() {
            when(appRepository.findByDeveloper_UserId(1L)).thenReturn(List.of(freeApp, paidApp));
            when(downloadRepository.getDownloadsByDay(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(List.of());
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(anyLong())).thenReturn(0L);
 
            Map<String, Object> dashboard = analyticsService.getDeveloperAnalytics(1L);
 
            @SuppressWarnings("unchecked")
            Map<String, Object> downloadsAnalytics = (Map<String, Object>) dashboard.get("downloadsAnalytics");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> perApp = (List<Map<String, Object>>) downloadsAnalytics.get("perApp");
 
            assertThat(perApp.get(0).get("appName")).isEqualTo("Paid App");
            assertThat(perApp.get(1).get("appName")).isEqualTo("Free App");
        }
 
        @Test
        @DisplayName("should merge daily download counts across multiple apps for the same day")
        void downloadsAnalytics_MergesDailyTrendAcrossApps() {
            when(appRepository.findByDeveloper_UserId(1L)).thenReturn(List.of(paidApp, freeApp));
            when(downloadRepository.getDownloadsByDay(eq(10L), any(LocalDateTime.class)))
            .thenReturn(List.<Object[]>of(new Object[]{"2026-06-15", 5L}));
    when(downloadRepository.getDownloadsByDay(eq(11L), any(LocalDateTime.class)))
            .thenReturn(List.<Object[]>of(new Object[]{"2026-06-15", 3L}));
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(anyLong())).thenReturn(0L);
 
            Map<String, Object> dashboard = analyticsService.getDeveloperAnalytics(1L);
 
            @SuppressWarnings("unchecked")
            Map<String, Object> downloadsAnalytics = (Map<String, Object>) dashboard.get("downloadsAnalytics");
            @SuppressWarnings("unchecked")
            Map<String, Long> trend = (Map<String, Long>) downloadsAnalytics.get("dailyTrend");
 
            assertThat(trend.get("2026-06-15")).isEqualTo(8L);
        }
    }
 
    @Nested
    @DisplayName("revenue insights (via getDeveloperAnalytics)")
    class RevenueInsights {
 
        @Test
        @DisplayName("should calculate revenue as price multiplied by downloads")
        void revenueInsights_CalculatesPerAppRevenue() {
            when(appRepository.findByDeveloper_UserId(1L)).thenReturn(List.of(paidApp));
            when(downloadRepository.getDownloadsByDay(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(List.of());
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(anyLong())).thenReturn(0L);
 
            Map<String, Object> dashboard = analyticsService.getDeveloperAnalytics(1L);
 
            @SuppressWarnings("unchecked")
            Map<String, Object> revenue = (Map<String, Object>) dashboard.get("revenueInsights");
            assertThat(revenue.get("totalRevenue")).isEqualTo(BigDecimal.valueOf(4.99).multiply(BigDecimal.valueOf(1000)));
        }
 
        @Test
        @DisplayName("should correctly split free vs paid app counts")
        void revenueInsights_SplitsFreeAndPaidApps() {
            when(appRepository.findByDeveloper_UserId(1L)).thenReturn(List.of(paidApp, freeApp));
            when(downloadRepository.getDownloadsByDay(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(List.of());
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(anyLong())).thenReturn(0L);
 
            Map<String, Object> dashboard = analyticsService.getDeveloperAnalytics(1L);
 
            @SuppressWarnings("unchecked")
            Map<String, Object> revenue = (Map<String, Object>) dashboard.get("revenueInsights");
            assertThat(revenue.get("freeAppsCount")).isEqualTo(1L);
            assertThat(revenue.get("paidAppsCount")).isEqualTo(1L);
        }
    }
 
    @Nested
    @DisplayName("engagement reports (via getDeveloperAnalytics)")
    class EngagementReports {
 
        @Test
        @DisplayName("should compute review counts and average rating per app")
        void engagementReports_ComputesReviewCountsAndRatings() {
            when(appRepository.findByDeveloper_UserId(1L)).thenReturn(List.of(paidApp));
            when(downloadRepository.getDownloadsByDay(anyLong(), any(LocalDateTime.class)))
                    .thenReturn(List.of());
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(10L)).thenReturn(25L);
 
            Map<String, Object> dashboard = analyticsService.getDeveloperAnalytics(1L);
 
            @SuppressWarnings("unchecked")
            Map<String, Object> engagement = (Map<String, Object>) dashboard.get("engagementReports");
            assertThat(engagement.get("totalReviews")).isEqualTo(25L);
        }
 
        @Test
        @DisplayName("should give an app zero engagement score when it has zero downloads")
        void engagementReports_ZeroDownloads_ZeroEngagementScore() {
            App zeroDownloadApp = App.builder()
                    .appId(12L)
                    .name("New App")
                    .description("Just published, no downloads yet at all")
                    .category(AppCategory.UTILITIES)
                    .price(BigDecimal.ZERO)
                    .status(AppStatus.APPROVED)
                    .averageRating(BigDecimal.ZERO)
                    .downloadCount(0L)
                    .developer(developer)
                    .build();
 
            when(appRepository.findByDeveloper_UserId(1L)).thenReturn(List.of(zeroDownloadApp));
            when(downloadRepository.getDownloadsByDay(anyLong(), any(LocalDateTime.class)))
            .thenReturn(List.of());
    when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(12L)).thenReturn(0L);

    Map<String, Object> dashboard = analyticsService.getDeveloperAnalytics(1L);

    @SuppressWarnings("unchecked")
    Map<String, Object> engagement = (Map<String, Object>) dashboard.get("engagementReports");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> perApp = (List<Map<String, Object>>) engagement.get("perApp");

    assertThat(perApp.get(0).get("engagementScore")).isEqualTo(0.0);
}

@Test
@DisplayName("should average ratings across apps, ignoring apps without a rating")
void engagementReports_AveragesRatingAcrossApps() {
    when(appRepository.findByDeveloper_UserId(1L)).thenReturn(List.of(paidApp, freeApp));
    when(downloadRepository.getDownloadsByDay(anyLong(), any(LocalDateTime.class)))
            .thenReturn(List.of());
    when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(anyLong())).thenReturn(0L);

    Map<String, Object> dashboard = analyticsService.getDeveloperAnalytics(1L);

    @SuppressWarnings("unchecked")
    Map<String, Object> engagement = (Map<String, Object>) dashboard.get("engagementReports");
    assertThat(engagement.get("averageRatingAcrossApps")).isEqualTo(3.75);
}
}
}
