package com.appverse.pattern.strategy;
 
import com.appverse.entity.App;
import com.appverse.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
 
import java.util.List;
 
/**
* Strategy Pattern — Concrete Strategy 2.
* Recommends apps based on highest average rating.
* Used when user has no download history.
*/
@Component
@RequiredArgsConstructor
@Slf4j
public class TopRatedRecommendationStrategy implements RecommendationStrategy {
 
    private final AppRepository appRepository;
 
    @Override
    public List<App> recommend(Long userId, int limit) {
        log.debug("Applying TopRatedRecommendationStrategy for user: {}", userId);
        return appRepository.findTopRatedApps(PageRequest.of(0, limit));
    }
 
    @Override
    public String getStrategyName() {
        return "TOP_RATED";
    }
}