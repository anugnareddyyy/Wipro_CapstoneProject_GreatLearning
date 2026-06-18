package com.appverse.service;
 
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
 
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
 
import com.appverse.dto.request.ReviewRequest;
import com.appverse.dto.response.ReviewResponse;
import com.appverse.entity.App;
import com.appverse.entity.AppCategory;
import com.appverse.entity.AppStatus;
import com.appverse.entity.ModerationStatus;
import com.appverse.entity.Review;
import com.appverse.entity.Role;
import com.appverse.entity.SentimentType;
import com.appverse.entity.User;
import com.appverse.exception.DuplicateResourceException;
import com.appverse.exception.ResourceNotFoundException;
import com.appverse.exception.UnauthorizedActionException;
import com.appverse.repository.AppRepository;
import com.appverse.repository.ReviewRepository;
import com.appverse.repository.UserRepository;
import com.appverse.service.impl.ReviewServiceImpl;
 
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTest {
 
    @Mock private ReviewRepository reviewRepository;
    @Mock private AppRepository appRepository;
    @Mock private UserRepository userRepository;
 
    @InjectMocks
    private ReviewServiceImpl reviewService;
 
    private User testUser;
    private App testApp;
 
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .username("testuser")
                .email("user@test.com")
                .role(Role.USER)
                .isActive(true)
                .build();
 
        testApp = App.builder()
                .appId(5L)
                .name("ReviewTest App")
                .description("App used for testing review functionality")
                .category(AppCategory.GAMING)
                .status(AppStatus.APPROVED)
                .developer(testUser)
                .averageRating(BigDecimal.ZERO)
                .reviewCount(0)
                .build();
    }
 
    // ===== CREATE REVIEW =====
 
    @Nested
    @DisplayName("createReview()")
    class CreateReview {
 
        @Test
        @DisplayName("should save review with POSITIVE sentiment for high rating and positive text")
        void createReview_PositiveRatingAndText_SentimentIsPositive() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(5);
            request.setReviewText("This is an amazing and excellent application. Love the fast and smooth experience. Perfect!");
 
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewRepository.findByApp_AppIdAndUser_UserId(5L, 1L)).thenReturn(Optional.empty());
            when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
                Review r = inv.getArgument(0);
                return Review.builder()
                        .reviewId(100L)
                        .app(testApp)
                        .user(testUser)
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .sentiment(r.getSentiment())
                        .predictedRating(r.getPredictedRating())
                        .isFlagged(r.getIsFlagged())
                        .isVisible(r.getIsVisible())
                        .moderationStatus(r.getModerationStatus())
                        .build();
            });
            when(reviewRepository.findByApp_AppIdAndIsVisibleTrue(5L))
                    .thenReturn(List.of());
            when(appRepository.save(any(App.class))).thenReturn(testApp);
 
            ReviewResponse result = reviewService.createReview(5L, request, 1L);
 
            assertThat(result).isNotNull();
            assertThat(result.getRating()).isEqualTo(5);
            assertThat(result.getSentiment()).isEqualTo(SentimentType.POSITIVE);
            verify(reviewRepository, times(1)).save(any(Review.class));
        }
 
        @Test
        @DisplayName("should save review with NEGATIVE sentiment for low rating and negative text")
        void createReview_NegativeRatingAndText_SentimentIsNegative() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(1);
            request.setReviewText("Worst app ever! Terrible and horrible experience. Crashes all the time, broken functionality.");
 
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewRepository.findByApp_AppIdAndUser_UserId(5L, 1L)).thenReturn(Optional.empty());
            when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
                Review r = inv.getArgument(0);
                return Review.builder()
                        .reviewId(101L)
                        .app(testApp)
                        .user(testUser)
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .sentiment(r.getSentiment())
                        .predictedRating(r.getPredictedRating())
                        .isFlagged(r.getIsFlagged())
                        .isVisible(r.getIsVisible())
                        .moderationStatus(r.getModerationStatus())
                        .build();
            });
            when(reviewRepository.findByApp_AppIdAndIsVisibleTrue(5L))
                    .thenReturn(List.of());
            when(appRepository.save(any(App.class))).thenReturn(testApp);
 
            ReviewResponse result = reviewService.createReview(5L, request, 1L);
 
            assertThat(result.getSentiment()).isEqualTo(SentimentType.NEGATIVE);
        }
 
        @Test
        @DisplayName("should throw DuplicateResourceException when user already reviewed app")
        void createReview_WhenAlreadyReviewed_ThrowsDuplicateResourceException() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(4);
            request.setReviewText("Great app with many features");
 
            Review existing = Review.builder().reviewId(77L).app(testApp).user(testUser).build();
 
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewRepository.findByApp_AppIdAndUser_UserId(5L, 1L))
                    .thenReturn(Optional.of(existing));
 
            assertThatThrownBy(() -> reviewService.createReview(5L, request, 1L))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already reviewed");
 
            verify(reviewRepository, never()).save(any());
        }
 
        @Test
        @DisplayName("should throw ResourceNotFoundException when app does not exist")
        void createReview_WhenAppNotFound_ThrowsResourceNotFoundException() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(3);
            request.setReviewText("Decent app");
 
            when(appRepository.findById(999L)).thenReturn(Optional.empty());
 
            assertThatThrownBy(() -> reviewService.createReview(999L, request, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("App");
 
            verify(reviewRepository, never()).save(any());
        }
 
        @Test
        @DisplayName("should throw ResourceNotFoundException when user does not exist")
        void createReview_WhenUserNotFound_ThrowsResourceNotFoundException() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(3);
            request.setReviewText("Decent app");
 
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());
 
            assertThatThrownBy(() -> reviewService.createReview(5L, request, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }
    }
 
    // ===== FAKE REVIEW DETECTION =====
 
    @Nested
    @DisplayName("Fake Review Detection (isLikelyFakeReview, via createReview)")
    class FakeReviewDetection {
 
        @Test
        @DisplayName("should flag review with very short text and extreme rating as suspicious")
        void createReview_ShortTextWithExtremeRating_FlagsAsFake() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(5);
            request.setReviewText("Best");
 
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewRepository.findByApp_AppIdAndUser_UserId(5L, 1L)).thenReturn(Optional.empty());
 
            ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
            when(reviewRepository.save(reviewCaptor.capture())).thenAnswer(inv -> {
                Review r = reviewCaptor.getValue();
                return Review.builder()
                        .reviewId(200L)
                        .app(r.getApp())
                        .user(r.getUser())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .sentiment(r.getSentiment())
                        .predictedRating(r.getPredictedRating())
                        .isFlagged(r.getIsFlagged())
                        .isVisible(r.getIsVisible())
                        .moderationStatus(r.getModerationStatus())
                        .build();
            });
            when(reviewRepository.findByApp_AppIdAndIsVisibleTrue(5L)).thenReturn(List.of());
            when(appRepository.save(any(App.class))).thenReturn(testApp);
 
            reviewService.createReview(5L, request, 1L);
 
            Review captured = reviewCaptor.getValue();
            assertThat(captured.getIsFlagged()).isTrue();
        }
 
        @Test
        @DisplayName("should NOT flag a normal-length review with rating matching sentiment")
        void createReview_NormalReviewMatchingRating_NotFlagged() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(4);
            request.setReviewText("Pretty good app overall, does what it says and works well most of the time.");
 
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewRepository.findByApp_AppIdAndUser_UserId(5L, 1L)).thenReturn(Optional.empty());
 
            ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
            when(reviewRepository.save(reviewCaptor.capture())).thenAnswer(inv -> {
                Review r = reviewCaptor.getValue();
                return Review.builder()
                        .reviewId(201L)
                        .app(r.getApp())
                        .user(r.getUser())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .sentiment(r.getSentiment())
                        .predictedRating(r.getPredictedRating())
                        .isFlagged(r.getIsFlagged())
                        .isVisible(r.getIsVisible())
                        .moderationStatus(r.getModerationStatus())
                        .build();
            });
            when(reviewRepository.findByApp_AppIdAndIsVisibleTrue(5L)).thenReturn(List.of());
            when(appRepository.save(any(App.class))).thenReturn(testApp);
 
            reviewService.createReview(5L, request, 1L);
 
            assertThat(reviewCaptor.getValue().getIsFlagged()).isFalse();
        }
    }
 
    // ===== UPDATE REVIEW =====
 
    @Nested
    @DisplayName("updateReview()")
    class UpdateReview {
 
        @Test
        @DisplayName("should update review when user is the author")
        void updateReview_WhenAuthor_UpdatesSuccessfully() {
            Review existing = Review.builder()
                    .reviewId(50L)
                    .rating(3)
                    .comment("Okay app")
                    .user(testUser)
                    .app(testApp)
                    .build();
 
            ReviewRequest request = new ReviewRequest();
            request.setRating(5);
            request.setReviewText("Actually it's excellent and amazing now!");
 
            when(reviewRepository.findById(50L)).thenReturn(Optional.of(existing));
            when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
            when(reviewRepository.findByApp_AppIdAndIsVisibleTrue(5L)).thenReturn(List.of());
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(appRepository.save(any(App.class))).thenReturn(testApp);
 
            ReviewResponse result = reviewService.updateReview(50L, request, 1L);
 
            assertThat(result.getRating()).isEqualTo(5);
            verify(reviewRepository).save(any(Review.class));
        }
 
        @Test
        @DisplayName("should throw UnauthorizedActionException when non-author tries to update")
        void updateReview_WhenNotAuthor_ThrowsUnauthorizedActionException() {
            Review existing = Review.builder()
                    .reviewId(50L)
                    .rating(3)
                    .comment("Okay app")
                    .user(testUser)
                    .app(testApp)
                    .build();
 
            ReviewRequest request = new ReviewRequest();
            request.setRating(5);
            request.setReviewText("Trying to edit someone else's review");
 
            when(reviewRepository.findById(50L)).thenReturn(Optional.of(existing));
 
            assertThatThrownBy(() -> reviewService.updateReview(50L, request, 99L))
                    .isInstanceOf(UnauthorizedActionException.class)
                    .hasMessageContaining("own");
 
            verify(reviewRepository, never()).save(any());
        }
 
        @Test
        @DisplayName("should throw ResourceNotFoundException when review does not exist")
        void updateReview_WhenReviewNotFound_ThrowsResourceNotFoundException() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(5);
            request.setReviewText("Some text");
 
            when(reviewRepository.findById(999L)).thenReturn(Optional.empty());
 
            assertThatThrownBy(() -> reviewService.updateReview(999L, request, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
 
    // ===== DELETE REVIEW =====
 
    @Nested
    @DisplayName("deleteReview()")
    class DeleteReview {
 
        @Test
        @DisplayName("should delete review when user is the author")
        void deleteReview_WhenAuthor_DeletesSuccessfully() {
            Review review = Review.builder()
                    .reviewId(50L)
                    .rating(4)
                    .comment("Good application overall")
                    .user(testUser)
                    .app(testApp)
                    .build();
 
            when(reviewRepository.findById(50L)).thenReturn(Optional.of(review));
            when(reviewRepository.findByApp_AppIdAndIsVisibleTrue(5L)).thenReturn(List.of());
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(appRepository.save(any())).thenReturn(testApp);
 
            assertThatCode(() -> reviewService.deleteReview(50L, 1L))
                    .doesNotThrowAnyException();
 
            verify(reviewRepository).delete(review);
        }
 
        @Test
        @DisplayName("should throw UnauthorizedActionException when non-author tries to delete")
        void deleteReview_WhenNotAuthor_ThrowsUnauthorizedActionException() {
            Review review = Review.builder()
                    .reviewId(50L)
                    .rating(4)
                    .user(testUser)
                    .app(testApp)
                    .build();
 
            when(reviewRepository.findById(50L)).thenReturn(Optional.of(review));
 
            assertThatThrownBy(() -> reviewService.deleteReview(50L, 99L))
                    .isInstanceOf(UnauthorizedActionException.class)
                    .hasMessageContaining("own");
 
            verify(reviewRepository, never()).delete(any());
        }
 
        @Test
        @DisplayName("should throw ResourceNotFoundException when review does not exist")
        void deleteReview_WhenReviewNotFound_ThrowsResourceNotFoundException() {
            when(reviewRepository.findById(999L)).thenReturn(Optional.empty());
 
            assertThatThrownBy(() -> reviewService.deleteReview(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
 
            verify(reviewRepository, never()).delete(any());
        }
    }
 
    // ===== GET APP REVIEWS =====
 
    @Nested
    @DisplayName("getAppReviews()")
    class GetAppReviews {
 
        @Test
        @DisplayName("should return paginated visible reviews for an app")
        void getAppReviews_ReturnsVisibleReviewsPage() {
            Review review = Review.builder()
                    .reviewId(1L)
                    .rating(5)
                    .comment("Great")
                    .user(testUser)
                    .app(testApp)
                    .isVisible(true)
                    .build();
 
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> page = new PageImpl<>(List.of(review));
 
            when(reviewRepository.findByApp_AppIdAndIsVisibleTrue(5L, pageable)).thenReturn(page);
 
            Page<ReviewResponse> result = reviewService.getAppReviews(5L, pageable);
 
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getRating()).isEqualTo(5);
        }
    }
 
    // ===== SENTIMENT ANALYSIS =====
 
    @Nested
    @DisplayName("getSentimentAnalysis()")
    class GetSentimentAnalysis {
 
        @Test
        @DisplayName("should return sentiment counts, percentages and total for existing app")
        void getSentimentAnalysis_ForExistingApp_ReturnsBreakdown() {
            when(reviewRepository.getSentimentBreakdown(5L)).thenReturn(
                    List.of(
                            new Object[]{"POSITIVE", 10L},
                            new Object[]{"NEGATIVE", 3L},
                            new Object[]{"NEUTRAL", 2L}
                    )
            );
 
            Map<String, Object> result = reviewService.getSentimentAnalysis(5L);
 
            assertThat(result).containsKeys("counts", "percentages", "totalReviews");
            assertThat(result.get("totalReviews")).isEqualTo(15L);
 
            @SuppressWarnings("unchecked")
            Map<String, Long> counts = (Map<String, Long>) result.get("counts");
            assertThat(counts.get("POSITIVE")).isEqualTo(10L);
            assertThat(counts.get("NEGATIVE")).isEqualTo(3L);
            assertThat(counts.get("NEUTRAL")).isEqualTo(2L);
        }
 
        @Test
        @DisplayName("should return zeroed breakdown when app has no reviews")
        void getSentimentAnalysis_WhenNoReviews_ReturnsZeroedBreakdown() {
            when(reviewRepository.getSentimentBreakdown(5L)).thenReturn(List.of());
 
            Map<String, Object> result = reviewService.getSentimentAnalysis(5L);
 
            assertThat(result.get("totalReviews")).isEqualTo(0L);
        }
    }
 
    // ===== RATING PREDICTION =====
 
    @Nested
    @DisplayName("predictRatingFromText()")
    class PredictRatingFromText {
 
        @Test
        @DisplayName("should return neutral 3.0 for blank text")
        void predictRatingFromText_BlankText_ReturnsNeutral() {
            assertThat(reviewService.predictRatingFromText("")).isEqualTo(3.0);
            assertThat(reviewService.predictRatingFromText(null)).isEqualTo(3.0);
        }
 
        @Test
        @DisplayName("should return higher score for strongly positive text")
        void predictRatingFromText_PositiveText_ReturnsHighScore() {
            Double result = reviewService.predictRatingFromText("This is excellent and amazing, just perfect!");
            assertThat(result).isGreaterThan(3.0);
            assertThat(result).isLessThanOrEqualTo(5.0);
        }
 
        @Test
        @DisplayName("should return lower score for strongly negative text")
        void predictRatingFromText_NegativeText_ReturnsLowScore() {
            Double result = reviewService.predictRatingFromText("Terrible, worst app, completely broken and useless");
            assertThat(result).isLessThan(3.0);
            assertThat(result).isGreaterThanOrEqualTo(1.0);
        }
 
        @Test
        @DisplayName("should clamp score to maximum 5.0 for extremely positive text")
        void predictRatingFromText_ExtremelyPositiveText_ClampsToFive() {
            Double result = reviewService.predictRatingFromText(
                    "excellent amazing perfect great best awesome love recommend helpful good");
            assertThat(result).isEqualTo(5.0);
        }
    }
 
    // ===== MODERATION =====
 
    @Nested
    @DisplayName("getFlaggedReviews() / moderateReview()")
    class Moderation {
 
        @Test
        @DisplayName("should return all flagged reviews")
        void getFlaggedReviews_ReturnsFlaggedList() {
            Review flagged = Review.builder()
                    .reviewId(1L)
                    .rating(1) 
                    .comment("bad")
                    .user(testUser)
                    .app(testApp)
                    .isFlagged(true)
                    .build();
 
            when(reviewRepository.findByIsFlaggedTrueOrderByCreatedAtDesc())
                    .thenReturn(List.of(flagged));
 
            List<ReviewResponse> result = reviewService.getFlaggedReviews();
 
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsFlagged()).isTrue();
        }
 
        @Test
        @DisplayName("should hide review and set status REMOVED when admin removes it")
        void moderateReview_WhenRemoved_HidesReview() {
            Review review = Review.builder()
                    .reviewId(1L)
                    .rating(1)
                    .comment("bad")
                    .user(testUser)
                    .app(testApp)
                    .isFlagged(true)
                    .isVisible(true)
                    .build();
 
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
            when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
            when(reviewRepository.findByApp_AppIdAndIsVisibleTrue(5L)).thenReturn(List.of());
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(appRepository.save(any(App.class))).thenReturn(testApp);
 
            ReviewResponse result = reviewService.moderateReview(1L, ModerationStatus.REMOVED, 9L);
 
            assertThat(result.getIsVisible()).isFalse();
        }
 
        @Test
        @DisplayName("should restore visibility and clear flag when admin approves")
        void moderateReview_WhenApproved_RestoresVisibilityAndClearsFlag() {
            Review review = Review.builder()
                    .reviewId(1L)
                    .rating(3)
                    .comment("borderline")
                    .user(testUser)
                    .app(testApp)
                    .isFlagged(true)
                    .isVisible(false)
                    .build();
 
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
            when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
            when(reviewRepository.findByApp_AppIdAndIsVisibleTrue(5L)).thenReturn(List.of());
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(appRepository.save(any(App.class))).thenReturn(testApp);
 
            ReviewResponse result = reviewService.moderateReview(1L, ModerationStatus.APPROVED, 9L);
 
            assertThat(result.getIsVisible()).isTrue();
            assertThat(result.getIsFlagged()).isFalse();
        }
 
        @Test
        @DisplayName("should throw ResourceNotFoundException when review does not exist")
        void moderateReview_WhenReviewNotFound_ThrowsResourceNotFoundException() {
            when(reviewRepository.findById(404L)).thenReturn(Optional.empty());
 
            assertThatThrownBy(() -> reviewService.moderateReview(404L, ModerationStatus.APPROVED, 9L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
 
    // ===== REVIEW DASHBOARD =====
 
    @Nested
    @DisplayName("getAllReviewsForDashboard()")
    class GetAllReviewsForDashboard {
 
        @Test
        @DisplayName("should query flagged-only reviews when flaggedOnly is true")
        void getAllReviewsForDashboard_FlaggedOnly_QueriesFlagged() {
            Pageable pageable = PageRequest.of(0, 10);
            when(reviewRepository.findByIsFlaggedTrue(pageable))
                    .thenReturn(new PageImpl<>(List.of()));
 
            reviewService.getAllReviewsForDashboard(null, null, true, pageable);
 
            verify(reviewRepository).findByIsFlaggedTrue(pageable);
        }
 
        @Test
        @DisplayName("should query by sentiment when sentiment filter is provided")
        void getAllReviewsForDashboard_BySentiment_QueriesSentiment() {
            Pageable pageable = PageRequest.of(0, 10);
            when(reviewRepository.findBySentiment(SentimentType.POSITIVE, pageable))
                    .thenReturn(new PageImpl<>(List.of()));
 
            reviewService.getAllReviewsForDashboard("positive", null, false, pageable);
 
            verify(reviewRepository).findBySentiment(SentimentType.POSITIVE, pageable);
        }
 
        @Test
        @DisplayName("should query by minimum rating when minRating is provided")
        void getAllReviewsForDashboard_ByMinRating_QueriesRatingThreshold() {
            Pageable pageable = PageRequest.of(0, 10);
            when(reviewRepository.findByRatingGreaterThanEqual(4, pageable))
                    .thenReturn(new PageImpl<>(List.of()));
 
            reviewService.getAllReviewsForDashboard(null, 4, false, pageable);
 
            verify(reviewRepository).findByRatingGreaterThanEqual(4, pageable);
        }
 
        @Test
        @DisplayName("should default to all reviews ordered by creation date when no filters given")
        void getAllReviewsForDashboard_NoFilters_QueriesAllOrderedByCreatedAt() {
            Pageable pageable = PageRequest.of(0, 10);
            when(reviewRepository.findAllByOrderByCreatedAtDesc(pageable))
                    .thenReturn(new PageImpl<>(List.of()));
 
            reviewService.getAllReviewsForDashboard(null, null, false, pageable);
 
            verify(reviewRepository).findAllByOrderByCreatedAtDesc(pageable);
        }
    }
}
 
              
 