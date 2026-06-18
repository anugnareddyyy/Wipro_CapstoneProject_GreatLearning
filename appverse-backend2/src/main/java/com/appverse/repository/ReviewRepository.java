package com.appverse.repository;
 
import com.appverse.entity.Review;
import com.appverse.entity.SentimentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
import java.util.Optional;
 
public interface ReviewRepository extends JpaRepository<Review, Long> {
 
    List<Review> findByApp_AppIdAndIsVisibleTrue(Long appId);
 
    Page<Review> findByApp_AppIdAndIsVisibleTrue(Long appId, Pageable pageable);
 
    long countByApp_AppIdAndIsVisibleTrue(Long appId);
 
    Optional<Review> findByApp_AppIdAndUser_UserId(Long appId, Long userId);
 
    List<Review> findByUser_UserId(Long userId);
 
    @Query("SELECT r.sentiment, COUNT(r) FROM Review r " +
           "WHERE r.app.appId = :appId AND r.isVisible = true " +
           "GROUP BY r.sentiment")
    List<Object[]> getSentimentBreakdown(@Param("appId") Long appId);
 
    // ===== Moderation =====
 
    List<Review> findByIsFlaggedTrueOrderByCreatedAtDesc();
 
    Page<Review> findByIsFlaggedTrue(Pageable pageable);
 
    // ===== Review Dashboard =====
 
    Page<Review> findBySentiment(SentimentType sentiment, Pageable pageable);
 
    Page<Review> findByRatingGreaterThanEqual(Integer rating, Pageable pageable);
 
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

	Object existsByUser_UserIdAndApp_AppId(long l, long m);
}
 