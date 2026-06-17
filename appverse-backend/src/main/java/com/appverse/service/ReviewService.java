package com.appverse.service;
 
import com.appverse.dto.request.ReviewRequest;
import com.appverse.dto.response.ReviewResponse;
import com.appverse.entity.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 
import java.util.List;
import java.util.Map;
 
public interface ReviewService {
 
    ReviewResponse createReview(Long appId, ReviewRequest request, Long userId);
 
    ReviewResponse updateReview(Long reviewId, ReviewRequest request, Long userId);
 
    void deleteReview(Long reviewId, Long userId);
 
    Page<ReviewResponse> getAppReviews(Long appId, Pageable pageable);
 
    Map<String, Object> getSentimentAnalysis(Long appId);
 
    /**
     * Predict the star rating implied by review text content,
     * independent of the rating the user actually selected.
     */
    Double predictRatingFromText(String reviewText);
 
    /**
     * Get all flagged reviews awaiting moderation.
     */
    List<ReviewResponse> getFlaggedReviews();
 
    /**
     * Approve or remove a flagged review.
     */
    ReviewResponse moderateReview(Long reviewId, ModerationStatus status, Long adminId);
 
    /**
     * Get all reviews across the platform for the admin Review Dashboard,
     * with optional filters.
     */
    Page<ReviewResponse> getAllReviewsForDashboard(
            String sentiment, Integer minRating, Boolean flaggedOnly, Pageable pageable);
}
 