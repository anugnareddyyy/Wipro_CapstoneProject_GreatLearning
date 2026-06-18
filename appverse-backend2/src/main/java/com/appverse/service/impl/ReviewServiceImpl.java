package com.appverse.service.impl;
 
import com.appverse.dto.request.ReviewRequest;
import com.appverse.dto.response.ReviewResponse;
import com.appverse.entity.*;
import com.appverse.exception.DuplicateResourceException;
import com.appverse.exception.ResourceNotFoundException;
import com.appverse.exception.UnauthorizedActionException;
import com.appverse.repository.AppRepository;
import com.appverse.repository.ReviewRepository;
import com.appverse.repository.UserRepository;
import com.appverse.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.*;
import java.util.stream.Collectors;
 
/**
* Implementation of ReviewService.
*
* Handles:
* - Review CRUD with ownership checks
* - Rule-based sentiment analysis (Module 5)
* - Rating prediction from review text
* - Fake-review flagging heuristics
* - Review moderation workflow (approve/remove)
* - Platform-wide review dashboard data
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
 
    private final ReviewRepository reviewRepository;
    private final AppRepository appRepository;
    private final UserRepository userRepository;
 
    // ===== Sentiment keyword maps =====
 
    private static final Set<String> POSITIVE_WORDS = Set.of(
            "excellent", "amazing", "love", "perfect", "great", "best",
            "awesome", "good", "helpful", "recommend", "fantastic",
            "wonderful", "easy", "smooth", "fast", "reliable"
    );
 
    private static final Set<String> NEGATIVE_WORDS = Set.of(
            "terrible", "worst", "hate", "awful", "useless", "broken",
            "crash", "bad", "disappointing", "waste", "bug", "slow",
            "annoying", "horrible", "fails"
    );
 
    // ===== Rating prediction keyword weights =====
 
    private static final Map<String, Double> POSITIVE_WEIGHTS = Map.ofEntries(
            Map.entry("excellent", 1.5), Map.entry("amazing", 1.5),
            Map.entry("love", 1.3), Map.entry("perfect", 1.5),
            Map.entry("great", 1.2), Map.entry("best", 1.3),
            Map.entry("awesome", 1.3), Map.entry("good", 0.8),
            Map.entry("helpful", 0.9), Map.entry("recommend", 1.0)
    );
     
    private static final Map<String, Double> NEGATIVE_WEIGHTS = Map.ofEntries(
            Map.entry("terrible", -1.5), Map.entry("worst", -1.5),
            Map.entry("hate", -1.3), Map.entry("awful", -1.4),
            Map.entry("useless", -1.2), Map.entry("broken", -1.1),
            Map.entry("crash", -1.0), Map.entry("bad", -0.8),
            Map.entry("disappointing", -1.1), Map.entry("waste", -1.2),
            Map.entry("bug", -0.7)
    );
 
    // ===== CRUD =====
 
    @Override
    @Transactional
    public ReviewResponse createReview(Long appId, ReviewRequest request, Long userId) {
        log.info("User {} creating review for app {}", userId, appId);
 
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App", "appId", appId));
 
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
 
        reviewRepository.findByApp_AppIdAndUser_UserId(appId, userId)
                .ifPresent(r -> {
                    throw new DuplicateResourceException("You have already reviewed this app");
                });
 
        SentimentType sentiment = analyzeSentiment(request.getReviewText(), request.getRating());
        Double predicted = predictRatingFromText(request.getReviewText());
        boolean flagged = isLikelyFakeReview(request.getReviewText(), request.getRating());
 
        Review review = Review.builder()
                .app(app)
                .user(user)
                .rating(request.getRating())
                .comment(request.getReviewText())
                .sentiment(sentiment)
                .predictedRating(predicted)
                .isFlagged(flagged)
                .isVisible(true)
                .moderationStatus(ModerationStatus.PENDING)
                .build();
 
        Review saved = reviewRepository.save(review);
        updateAppRatingStats(appId);
 
        log.info("Review {} created - sentiment: {}, flagged: {}",
                saved.getReviewId(), sentiment, flagged);
 
        return mapToResponse(saved);
    }
 
    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "reviewId", reviewId));
 
        if (!review.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedActionException("You can only edit your own reviews");
        }
 
        review.setRating(request.getRating());
        review.setComment(request.getReviewText());
        review.setSentiment(analyzeSentiment(request.getReviewText(), request.getRating()));
        review.setPredictedRating(predictRatingFromText(request.getReviewText()));
 
        Review saved = reviewRepository.save(review);
        updateAppRatingStats(review.getApp().getAppId());
 
        return mapToResponse(saved);
    }
 
    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "reviewId", reviewId));
 
        if (!review.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedActionException("You can only delete your own reviews");
        }
 
        Long appId = review.getApp().getAppId();
        reviewRepository.delete(review);
        updateAppRatingStats(appId);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAppReviews(Long appId, Pageable pageable) {
        return reviewRepository
                .findByApp_AppIdAndIsVisibleTrue(appId, pageable)
                .map(this::mapToResponse);
    }
 
    // ===== Sentiment Analysis =====
 
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSentimentAnalysis(Long appId) {
        List<Object[]> breakdown = reviewRepository.getSentimentBreakdown(appId);
 
        Map<String, Long> counts = new HashMap<>();
        counts.put("POSITIVE", 0L);
        counts.put("NEUTRAL", 0L);
        counts.put("NEGATIVE", 0L);
 
        long total = 0;
        for (Object[] row : breakdown) {
            String sentiment = row[0].toString();
            Long count = (Long) row[1];
            counts.put(sentiment, count);
            total += count;
        }
 
        Map<String, Double> percentages = new HashMap<>();
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            double pct = total > 0 ? (entry.getValue() * 100.0 / total) : 0.0;
            percentages.put(entry.getKey(), Math.round(pct * 10) / 10.0);
        }
 
        Map<String, Object> result = new HashMap<>();
        result.put("counts", counts);
        result.put("percentages", percentages);
        result.put("totalReviews", total);
 
        return result;
    }
 
    private SentimentType analyzeSentiment(String comment, Integer rating) {
        if (comment == null || comment.isBlank()) {
            return rating >= 4 ? SentimentType.POSITIVE
                 : rating <= 2 ? SentimentType.NEGATIVE
                 : SentimentType.NEUTRAL;
        }
 
        String lower = comment.toLowerCase();
        int posScore = 0, negScore = 0;
 
        for (String word : POSITIVE_WORDS) {
            if (lower.contains(word)) posScore++;
        }
        for (String word : NEGATIVE_WORDS) {
            if (lower.contains(word)) negScore++;
        }
 
        if (rating >= 4) posScore += 2;
        if (rating <= 2) negScore += 2;
 
        if (posScore > negScore) return SentimentType.POSITIVE;
        if (negScore > posScore) return SentimentType.NEGATIVE;
        return SentimentType.NEUTRAL;
    }
 
    // ===== Rating Prediction =====
 
    @Override
    public Double predictRatingFromText(String reviewText) {
        if (reviewText == null || reviewText.isBlank()) {
            return 3.0;
        }
 
        String lower = reviewText.toLowerCase();
        double score = 3.0;
 
        for (Map.Entry<String, Double> entry : POSITIVE_WEIGHTS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                score += entry.getValue();
            }
        }
        for (Map.Entry<String, Double> entry : NEGATIVE_WEIGHTS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                score += entry.getValue();
            }
        }
 
        score = Math.max(1.0, Math.min(5.0, score));
        return Math.round(score * 10) / 10.0;
    }
 
    // ===== Fake Review Detection =====
 
    private boolean isLikelyFakeReview(String comment, Integer rating) {
        if (comment == null || comment.isBlank()) {
            return false;
        }
 
        boolean tooShort = comment.trim().length() < 10;
        boolean extremeRating = rating == 1 || rating == 5;
 
        Double predicted = predictRatingFromText(comment);
        boolean bigMismatch = Math.abs(predicted - rating) > 2.5;
 
        boolean spammyPattern = comment.matches(".*([a-zA-Z])\\1{4,}.*");
 
        return (tooShort && extremeRating) || bigMismatch || spammyPattern;
    }
 
    // ===== Moderation =====
 
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReviewResponse> getFlaggedReviews() {
        log.debug("Fetching flagged reviews for moderation");
        return reviewRepository.findByIsFlaggedTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ReviewResponse moderateReview(Long reviewId, ModerationStatus status, Long adminId) {
        log.info("Admin {} moderating review {} -> {}", adminId, reviewId, status);
 
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "reviewId", reviewId));
 
        review.setModerationStatus(status);
 
        if (status == ModerationStatus.REMOVED) {
            review.setIsVisible(false);
        } else if (status == ModerationStatus.APPROVED) {
            review.setIsVisible(true);
            review.setIsFlagged(false);
        }
 
        Review saved = reviewRepository.save(review);
        updateAppRatingStats(saved.getApp().getAppId());
 
        log.info("Review {} moderation complete - status: {}, visible: {}",
                reviewId, status, saved.getIsVisible());
 
        return mapToResponse(saved);
    }
 
    // ===== Review Dashboard =====
 
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ReviewResponse> getAllReviewsForDashboard(
            String sentiment, Integer minRating, Boolean flaggedOnly, Pageable pageable) {
 
        log.debug("Loading review dashboard - sentiment: {}, minRating: {}, flagged: {}",
                sentiment, minRating, flaggedOnly);
 
        Page<Review> reviews;
 
        if (Boolean.TRUE.equals(flaggedOnly)) {
            reviews = reviewRepository.findByIsFlaggedTrue(pageable);
        } else if (sentiment != null && !sentiment.isBlank()) {
            reviews = reviewRepository.findBySentiment(
                    SentimentType.valueOf(sentiment.toUpperCase()), pageable);
        } else if (minRating != null) {
            reviews = reviewRepository.findByRatingGreaterThanEqual(minRating, pageable);
        } else {
            reviews = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
 
        return reviews.map(this::mapToResponse);
    }
 
    // ===== Helpers =====
 
    private void updateAppRatingStats(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App", "appId", appId));
 
        List<Review> visibleReviews = reviewRepository.findByApp_AppIdAndIsVisibleTrue(appId);
 
        if (visibleReviews.isEmpty()) {
            app.setAverageRating(java.math.BigDecimal.ZERO);
            app.setReviewCount(0);
        } else {
            double avg = visibleReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            app.setAverageRating(java.math.BigDecimal.valueOf(Math.round(avg * 10) / 10.0));
            app.setReviewCount(visibleReviews.size());
        }
 
        appRepository.save(app);
    }
 
    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .appId(review.getApp().getAppId())
                .appName(review.getApp().getName())
                .userId(review.getUser().getUserId())
                .userName(review.getUser().getUsername())
                .rating(review.getRating())
                .comment(review.getComment())
                .sentiment(review.getSentiment())
                .predictedRating(review.getPredictedRating())
                .isFlagged(review.getIsFlagged())
                .isVisible(review.getIsVisible())
                .moderationStatus(review.getModerationStatus())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
 