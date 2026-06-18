//package com.appverse.dto.response;
//
//import com.appverse.entity.SentimentType;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//
///**
// * DTO for review data returned to the frontend.
// */
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class ReviewResponse {
//
//    private Long reviewId;
//    private Integer rating;
//    private String title;
//    private String reviewText;
//    private SentimentType sentiment;
//    private Double sentimentScore;
//    private Boolean isFakeFlagged;
//    private Integer helpfulCount;
//
//    // Author info (flattened)
//    private Long userId;
//    private String username;
//    private String avatarUrl;
//
//    // App info (flattened)
//    private Long appId;
//    private String appName;
//
//    private LocalDateTime createdAt;
//}

package com.appverse.dto.response;
 
import com.appverse.entity.ModerationStatus;
import com.appverse.entity.SentimentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
 
    private Long reviewId;
    private Long appId;
    private String appName;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private SentimentType sentiment;
    private Double predictedRating;
    private Boolean isFlagged;
    private Boolean isVisible;
    private ModerationStatus moderationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
 
