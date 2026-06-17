package com.appverse.pattern.strategy;
 
import com.appverse.entity.App;
import com.appverse.entity.AppStatus;
import com.appverse.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
 
import java.util.List;
 
/**
* Strategy Pattern — Concrete Strategy 1.
* Recommends apps based on trending download count.
* Used for new users (cold start problem).
*/
@Component
@RequiredArgsConstructor
@Slf4j
public class TrendingRecommendationStrategy implements RecommendationStrategy {
 
    private final AppRepository appRepository;
 
    @Override
    public List<App> recommend(Long userId, int limit) {
        log.debug("Applying TrendingRecommendationStrategy for user: {}", userId);
        return appRepository.findTopTrendingApps(PageRequest.of(0, limit));
    }
 
    @Override
    public String getStrategyName() {
        return "TRENDING";
    }
}