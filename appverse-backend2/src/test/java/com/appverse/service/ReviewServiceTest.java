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

import com.appverse.dto.request.ReviewRequest;
import com.appverse.dto.response.ReviewResponse;
import com.appverse.entity.App;
import com.appverse.entity.AppCategory;
import com.appverse.entity.AppStatus;
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

/**
 * Unit tests for {@link ReviewServiceImpl}.
 *
 * Tests cover:
 * - Review submission with AI sentiment analysis
 * - Fake review detection logic
 * - Sentiment analytics aggregation
 * - Business rules (one review per user per app)
 * - Authorization checks (edit/delete own reviews only)
 */
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

    // ===== SUBMIT REVIEW =====

    @Nested
    @DisplayName("submitReview()")
    class SubmitReview {

        @Test
        @DisplayName("should save review with POSITIVE sentiment for high rating and positive text")
        void submitReview_PositiveRatingAndText_SentimentIsPositive() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(5);
            request.setTitle("Excellent app!");
            request.setReviewText("This is an amazing and excellent application. Love the fast and smooth experience. Perfect!");

            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewRepository.existsByUser_UserIdAndApp_AppId(1L, 5L)).thenReturn(false);
            when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
                Review r = inv.getArgument(0);
                // Simulate DB assigning an ID
                return Review.builder()
                        .reviewId(100L)
                        .rating(r.getRating())
                        .title(r.getTitle())
                        .reviewText(r.getReviewText())
                        .sentiment(r.getSentiment())
                        .sentimentScore(r.getSentimentScore())
                        .isFakeFlagged(r.getIsFakeFlagged())
                        .isVisible(r.getIsVisible())
                        .helpfulCount(0)
                        .user(testUser)
                        .app(testApp)
                        .build();
            });
            when(reviewRepository.calculateAverageRating(5L)).thenReturn(5.0);
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(5L)).thenReturn(1L);
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(appRepository.save(any(App.class))).thenReturn(testApp);

            ReviewResponse result = reviewService.submitReview(5L, request, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getRating()).isEqualTo(5);
            assertThat(result.getSentiment()).isEqualTo(SentimentType.POSITIVE);
            verify(reviewRepository, times(1)).save(any(Review.class));
        }

        @Test
        @DisplayName("should save review with NEGATIVE sentiment for low rating and negative text")
        void submitReview_NegativeRatingAndText_SentimentIsNegative() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(1);
            request.setTitle("Terrible!");
            request.setReviewText("Worst app ever! Terrible and horrible experience. Crashes all the time, broken functionality.");

            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewRepository.existsByUser_UserIdAndApp_AppId(1L, 5L)).thenReturn(false);
            when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
                Review r = inv.getArgument(0);
                return Review.builder()
                        .reviewId(101L)
                        .rating(r.getRating())
                        .sentiment(r.getSentiment())
                        .sentimentScore(r.getSentimentScore())
                        .isFakeFlagged(r.getIsFakeFlagged())
                        .isVisible(r.getIsVisible())
                        .helpfulCount(0)
                        .user(testUser)
                        .app(testApp)
                        .build();
            });
            when(reviewRepository.calculateAverageRating(5L)).thenReturn(1.0);
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(5L)).thenReturn(1L);
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(appRepository.save(any(App.class))).thenReturn(testApp);

            ReviewResponse result = reviewService.submitReview(5L, request, 1L);

            assertThat(result.getSentiment()).isEqualTo(SentimentType.NEGATIVE);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when user already reviewed app")
        void submitReview_WhenAlreadyReviewed_ThrowsDuplicateResourceException() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(4);
            request.setReviewText("Great app with many features");

            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewRepository.existsByUser_UserIdAndApp_AppId(1L, 5L)).thenReturn(true);

            assertThatThrownBy(() -> reviewService.submitReview(5L, request, 1L))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already submitted");

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when app does not exist")
        void submitReview_WhenAppNotFound_ThrowsResourceNotFoundException() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(3);

            when(appRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.submitReview(999L, request, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("App");
        }
    }

    // ===== FAKE REVIEW DETECTION =====

    @Nested
    @DisplayName("Fake Review Detection (AI)")
    class FakeReviewDetection {

        @Test
        @DisplayName("should flag review with very short text and extreme rating as suspicious")
        void submitReview_ShortTextWithExtremeRating_FlagsAsFake() {
            ReviewRequest request = new ReviewRequest();
            request.setRating(5);
            request.setReviewText("Best"); // only 1 word — suspicious

            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(reviewRepository.existsByUser_UserIdAndApp_AppId(1L, 5L)).thenReturn(false);

            ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
            when(reviewRepository.save(reviewCaptor.capture())).thenAnswer(inv -> {
                Review r = reviewCaptor.getValue();
                return Review.builder()
                        .reviewId(200L)
                        .rating(r.getRating())
                        .sentiment(r.getSentiment())
                        .isFakeFlagged(r.getIsFakeFlagged())
                        .isVisible(r.getIsVisible())
                        .helpfulCount(0)
                        .user(testUser)
                        .app(testApp)
                        .build();
            });
            when(reviewRepository.calculateAverageRating(5L)).thenReturn(5.0);
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(5L)).thenReturn(0L);
            when(appRepository.findById(5L)).thenReturn(Optional.of(testApp));
            when(appRepository.save(any(App.class))).thenReturn(testApp);

            reviewService.submitReview(5L, request, 1L);

            // The captured review should be flagged as fake
            Review capturedReview = reviewCaptor.getValue();
            assertThat(capturedReview.getIsFakeFlagged()).isTrue();
            assertThat(capturedReview.getIsVisible()).isFalse(); // auto-hidden
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
                    .reviewText("Good application overall")
                    .user(testUser)
                    .app(testApp)
                    .build();

            when(reviewRepository.findById(50L)).thenReturn(Optional.of(review));
            when(reviewRepository.calculateAverageRating(5L)).thenReturn(null);
            when(reviewRepository.countByApp_AppIdAndIsVisibleTrue(5L)).thenReturn(0L);
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
                    .user(testUser) // owned by userId=1
                    .app(testApp)
                    .build();

            when(reviewRepository.findById(50L)).thenReturn(Optional.of(review));

            assertThatThrownBy(() -> reviewService.deleteReview(50L, 99L)) // different userId
                    .isInstanceOf(UnauthorizedActionException.class)
                    .hasMessageContaining("own");

            verify(reviewRepository, never()).delete(any());
        }
    }

    // ===== SENTIMENT ANALYSIS =====

    @Nested
    @DisplayName("getSentimentAnalysis()")
    class GetSentimentAnalysis {

        @Test
        @DisplayName("should return sentiment distribution map for existing app")
        void getSentimentAnalysis_ForExistingApp_ReturnsDistributionMap() {
            when(appRepository.existsById(5L)).thenReturn(true);
            when(reviewRepository.countBySentimentForApp(5L)).thenReturn(
                List.of(
                    new Object[]{SentimentType.POSITIVE, 10L},
                    new Object[]{SentimentType.NEGATIVE, 3L},
                    new Object[]{SentimentType.NEUTRAL, 2L}
                )
            );
            when(reviewRepository.calculateAverageRating(5L)).thenReturn(4.2);

            Map<String, Object> result = reviewService.getSentimentAnalysis(5L);

            assertThat(result).isNotNull();
            assertThat(result).containsKey("sentimentDistribution");
            assertThat(result).containsKey("averageRating");
            assertThat(result.get("totalReviews")).isEqualTo(15L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when app does not exist")
        void getSentimentAnalysis_WhenAppNotFound_ThrowsResourceNotFoundException() {
            when(appRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> reviewService.getSentimentAnalysis(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
