package com.appverse.controller;
 
import com.appverse.dto.request.ReviewRequest;
import com.appverse.dto.response.ReviewResponse;
import com.appverse.entity.ModerationStatus;
import com.appverse.repository.UserRepository;
import com.appverse.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reviews", description = "Review CRUD, sentiment analysis, moderation, and dashboard")
public class ReviewController {
 
    private final ReviewService reviewService;
    private final UserRepository userRepository;
 
    private Long currentUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUserId();
    }
 
    @PostMapping("/app/{appId}")
    @Operation(summary = "Create a review for an app", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long appId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
 
        Long userId = currentUserId(userDetails);
        return ResponseEntity.ok(reviewService.createReview(appId, request, userId));
    }
 
    @PutMapping("/{reviewId}")
    @Operation(summary = "Update your own review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
 
        Long userId = currentUserId(userDetails);
        return ResponseEntity.ok(reviewService.updateReview(reviewId, request, userId));
    }
 
    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete your own review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
 
        Long userId = currentUserId(userDetails);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
 
    @GetMapping("/app/{appId}")
    @Operation(summary = "Get all reviews for an app")
    public ResponseEntity<Page<ReviewResponse>> getAppReviews(
            @PathVariable Long appId,
            @PageableDefault(size = 10) Pageable pageable) {
 
        return ResponseEntity.ok(reviewService.getAppReviews(appId, pageable));
    }
 
    @GetMapping("/app/{appId}/sentiment")
    @Operation(summary = "Get AI sentiment analysis breakdown for an app")
    public ResponseEntity<Map<String, Object>> getSentimentAnalysis(@PathVariable Long appId) {
        return ResponseEntity.ok(reviewService.getSentimentAnalysis(appId));
    }
 
    @PostMapping("/predict-rating")
    @Operation(summary = "Predict star rating implied by review text")
    public ResponseEntity<Map<String, Object>> predictRating(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        Double predicted = reviewService.predictRatingFromText(text);
 
        Map<String, Object> result = new HashMap<>();
        result.put("predictedRating", predicted);
        result.put("text", text);
 
        return ResponseEntity.ok(result);
    }
 
    // ===== Moderation (Admin only) =====
 
    @GetMapping("/flagged")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all flagged reviews pending moderation",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ReviewResponse>> getFlaggedReviews() {
        return ResponseEntity.ok(reviewService.getFlaggedReviews());
    }
 
    @PatchMapping("/{reviewId}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve or remove a flagged review",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ReviewResponse> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam ModerationStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
 
        Long adminId = currentUserId(userDetails);
        return ResponseEntity.ok(reviewService.moderateReview(reviewId, status, adminId));
    }
 
    // ===== Review Dashboard (Admin only) =====
 
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all reviews for the admin review dashboard",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<ReviewResponse>> getReviewDashboard(
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Boolean flaggedOnly,
            @PageableDefault(size = 20) Pageable pageable) {
 
        return ResponseEntity.ok(
                reviewService.getAllReviewsForDashboard(sentiment, minRating, flaggedOnly, pageable));
    }
}
 