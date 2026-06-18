package com.appverse.service;
 
import com.appverse.dto.response.AppResponse;
import com.appverse.entity.App;
import com.appverse.entity.AppCategory;
import com.appverse.entity.AppStatus;
import com.appverse.entity.Download;
import com.appverse.entity.Role;
import com.appverse.entity.User;
import com.appverse.pattern.strategy.RecommendationContext;
import com.appverse.pattern.strategy.TopRatedRecommendationStrategy;
import com.appverse.pattern.strategy.TrendingRecommendationStrategy;
import com.appverse.repository.AppRepository;
import com.appverse.repository.DownloadRepository;
import com.appverse.repository.UserRepository;
import com.appverse.service.impl.RecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
 
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
 
@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService Unit Tests")
class RecommendationServiceTest {
 
    @Mock private AppRepository appRepository;
    @Mock private UserRepository userRepository;
    @Mock private DownloadRepository downloadRepository;
    @Mock private AppService appService;
    @Mock private RecommendationContext recommendationContext;
    @Mock private TrendingRecommendationStrategy trendingStrategy;
    @Mock private TopRatedRecommendationStrategy topRatedStrategy;
 
    @InjectMocks
    private RecommendationServiceImpl recommendationService;
 
    private User user;
    private App app1;
    private App app2;
    private AppResponse appResponse1;
    private AppResponse appResponse2;
 
    @BeforeEach
    void setUp() {
        user = User.builder().userId(1L).username("testuser").role(Role.USER).isActive(true).build();
 
        app1 = App.builder()
                .appId(100L)
                .name("App One")
                .category(AppCategory.PRODUCTIVITY)
                .averageRating(BigDecimal.valueOf(4.5))
                .downloadCount(500L)
                .build();
 
        app2 = App.builder()
                .appId(200L)
                .name("App Two")
                .category(AppCategory.GAMING)
                .averageRating(BigDecimal.valueOf(4.0))
                .downloadCount(300L)
                .build();
 
        appResponse1 = AppResponse.builder()
                .appId(100L).name("App One").category(AppCategory.PRODUCTIVITY)
                .averageRating(BigDecimal.valueOf(4.5)).downloadCount(500L).build();
 
        appResponse2 = AppResponse.builder()
                .appId(200L).name("App Two").category(AppCategory.GAMING)
                .averageRating(BigDecimal.valueOf(4.0)).downloadCount(300L).build();
    }
 
    @Nested
    @DisplayName("getPersonalizedRecommendations()")
    class GetPersonalizedRecommendations {
 
        @Test
        @DisplayName("should select TrendingStrategy for a cold-start user with no download history")
        void getPersonalizedRecommendations_ColdStartUser_SelectsTrendingStrategy() {
            when(downloadRepository.findByUser_UserId(1L)).thenReturn(List.of());
            when(recommendationContext.executeStrategy(1L, 5)).thenReturn(List.of(app1));
            when(appService.getAppById(100L)).thenReturn(appResponse1);
 
            List<AppResponse> result = recommendationService.getPersonalizedRecommendations(1L, 5);
 
            verify(recommendationContext).setStrategy(trendingStrategy);
            verify(recommendationContext, never()).setStrategy(topRatedStrategy);
            assertThat(result).hasSize(1);
        }
 
        @Test
        @DisplayName("should select TopRatedStrategy for a user with existing download history")
        void getPersonalizedRecommendations_ActiveUser_SelectsTopRatedStrategy() {
            Download pastDownload = Download.builder().app(app2).user(user).build();
            when(downloadRepository.findByUser_UserId(1L)).thenReturn(List.of(pastDownload));
            when(recommendationContext.executeStrategy(1L, 5)).thenReturn(List.of(app1));
            when(appService.getAppById(100L)).thenReturn(appResponse1);
 
            recommendationService.getPersonalizedRecommendations(1L, 5);
 
            verify(recommendationContext).setStrategy(topRatedStrategy);
            verify(recommendationContext, never()).setStrategy(trendingStrategy);
        }
 
        @Test
        @DisplayName("should exclude apps the user has already downloaded")
        void getPersonalizedRecommendations_ExcludesAlreadyDownloadedApps() {
            Download pastDownload = Download.builder().app(app1).user(user).build();
            when(downloadRepository.findByUser_UserId(1L)).thenReturn(List.of(pastDownload));
            when(recommendationContext.executeStrategy(1L, 5)).thenReturn(List.of(app1, app2));
            when(appService.getAppById(200L)).thenReturn(appResponse2);
 
            List<AppResponse> result = recommendationService.getPersonalizedRecommendations(1L, 5);
 
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAppId()).isEqualTo(200L);
            verify(appService, never()).getAppById(100L);
        }
 
        @Test
        @DisplayName("should fall back to trending apps when the strategy returns no results")
        void getPersonalizedRecommendations_StrategyReturnsEmpty_FallsBackToTrending() {
            when(downloadRepository.findByUser_UserId(1L)).thenReturn(List.of());
            when(recommendationContext.executeStrategy(1L, 5)).thenReturn(List.of());
            when(appService.getTrendingApps(5)).thenReturn(List.of(appResponse1));
 
            List<AppResponse> result = recommendationService.getPersonalizedRecommendations(1L, 5);
 
            assertThat(result).containsExactly(appResponse1);
            verify(appService).getTrendingApps(5);
        }
 
        @Test
        @DisplayName("should respect the limit parameter when truncating results")
        void getPersonalizedRecommendations_RespectsLimit() {
            when(downloadRepository.findByUser_UserId(1L)).thenReturn(List.of());
            when(recommendationContext.executeStrategy(1L, 1)).thenReturn(List.of(app1, app2));
            when(appService.getAppById(100L)).thenReturn(appResponse1);
 
            List<AppResponse> result = recommendationService.getPersonalizedRecommendations(1L, 1);
 
            assertThat(result).hasSize(1);
        }
    }
 
    @Nested
    @DisplayName("getTrendingAnalysis()")
    class GetTrendingAnalysis {
 
        @Test
        @DisplayName("should group trending apps by category and identify the top category")
        void getTrendingAnalysis_GroupsByCategoryAndFindsTop() {
            when(appService.getTrendingApps(10)).thenReturn(List.of(appResponse1, appResponse2));
            when(downloadRepository.count()).thenReturn(1000L);
 
            Map<String, Object> analysis = recommendationService.getTrendingAnalysis();
 
            assertThat(analysis).containsKeys(
                    "trendingApps", "trendingByCategory", "totalPlatformDownloads", "topCategory", "generatedAt");
            assertThat(analysis.get("totalPlatformDownloads")).isEqualTo(1000L);
        }
 
        @Test
        @DisplayName("should return N/A as top category when there are no trending apps")
        void getTrendingAnalysis_NoTrendingApps_TopCategoryIsNA() {
            when(appService.getTrendingApps(10)).thenReturn(List.of());
            when(downloadRepository.count()).thenReturn(0L);
 
            Map<String, Object> analysis = recommendationService.getTrendingAnalysis();
 
            assertThat(analysis.get("topCategory")).isEqualTo("N/A");
        }
    }
 
    @Nested
    @DisplayName("predictDownloads()")
    class PredictDownloads {
 
        @Test
        @DisplayName("should compute a higher predicted growth for highly rated apps in fast-growing categories")
        void predictDownloads_HighRatingFastCategory_PredictsGrowth() {
            AppResponse gamingApp = AppResponse.builder()
                    .appId(300L)
                    .name("Hot Game")
                    .category(AppCategory.GAMING)
                    .averageRating(BigDecimal.valueOf(5.0))
                    .downloadCount(2000L)
                    .build();
 
            when(appService.getAppById(300L)).thenReturn(gamingApp);
            when(recommendationContext.getCurrentStrategyName()).thenReturn("TopRatedRecommendationStrategy");
 
            Map<String, Object> prediction = recommendationService.predictDownloads(300L);
 
            assertThat(prediction.get("currentDownloads")).isEqualTo(2000L);
            assertThat((Long) prediction.get("predicted30DayDownloads")).isGreaterThan(2000L);
            assertThat(prediction.get("confidenceScore")).isEqualTo(0.85);
        }
 
        @Test
        @DisplayName("should use medium confidence tier for apps with between 100 and 1000 downloads")
        void predictDownloads_MediumDownloadVolume_MediumConfidence() {
            AppResponse midApp = AppResponse.builder()
                    .appId(301L)
                    .name("Mid App")
                    .category(AppCategory.FINANCE)
                    .averageRating(BigDecimal.valueOf(3.5))
                    .downloadCount(500L)
                    .build();
 
            when(appService.getAppById(301L)).thenReturn(midApp);
            when(recommendationContext.getCurrentStrategyName()).thenReturn("TrendingRecommendationStrategy");
 
            Map<String, Object> prediction = recommendationService.predictDownloads(301L);
 
            assertThat(prediction.get("confidenceScore")).isEqualTo(0.70);
        }
 
        @Test
        @DisplayName("should use low confidence tier for apps with 100 or fewer downloads")
        void predictDownloads_LowDownloadVolume_LowConfidence() {
            AppResponse newApp = AppResponse.builder()
                    .appId(302L)
                    .name("Brand New App")
                    .category(AppCategory.UTILITIES)
                    .averageRating(null)
                    .downloadCount(10L)
                    .build();
 
            when(appService.getAppById(302L)).thenReturn(newApp);
            when(recommendationContext.getCurrentStrategyName()).thenReturn("TrendingRecommendationStrategy");
 
            Map<String, Object> prediction = recommendationService.predictDownloads(302L);
 
            assertThat(prediction.get("confidenceScore")).isEqualTo(0.50);
            assertThat(prediction.get("ratingMultiplier")).isEqualTo(0.8);
        }
 
        @Test
        @DisplayName("should treat null download count as zero")
        void predictDownloads_NullDownloadCount_TreatedAsZero() {
            AppResponse zeroApp = AppResponse.builder()
                    .appId(303L)
                    .name("Unreleased App")
                    .category(AppCategory.SOCIAL)
                    .averageRating(BigDecimal.valueOf(4.0))
                    .downloadCount(null)
                    .build();
 
            when(appService.getAppById(303L)).thenReturn(zeroApp);
            when(recommendationContext.getCurrentStrategyName()).thenReturn("TrendingRecommendationStrategy");
 
            Map<String, Object> prediction = recommendationService.predictDownloads(303L);
 
            assertThat(prediction.get("currentDownloads")).isEqualTo(0L);
            assertThat(prediction.get("predicted30DayDownloads")).isEqualTo(0L);
        }
    }
 
    @Nested
    @DisplayName("getCategoryInsights()")
    class GetCategoryInsights {
 
        @Test
        @DisplayName("should build app counts by category and identify fastest growing category")
        void getCategoryInsights_BuildsCountsAndFastestGrowing() {
            when(appRepository.countAppsByCategory()).thenReturn(
                    List.of(
                            new Object[]{AppCategory.GAMING, 50L},
                            new Object[]{AppCategory.PRODUCTIVITY, 30L}
                    )
            );
            when(recommendationContext.getCurrentStrategyName()).thenReturn("TrendingRecommendationStrategy");
 
            Map<String, Object> insights = recommendationService.getCategoryInsights();
 
            assertThat(insights).containsKeys(
                    "appCountByCategory", "categoryGrowthCoefficients",
                    "fastestGrowingCategory", "currentStrategy", "generatedAt");
            assertThat(insights.get("fastestGrowingCategory")).isEqualTo("GAMING");
        }
    }
}
 