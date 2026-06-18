package com.appverse.pattern.strategy;
 
import com.appverse.entity.App;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
import java.util.List;
 
/**
* Strategy Pattern — Context class.
* Holds a reference to the current strategy and delegates
* recommendation calls to it.
*
* The strategy is selected dynamically based on user profile:
* - New user (no downloads) → TrendingRecommendationStrategy
* - Active user            → TopRatedRecommendationStrategy
*/
@Component
@Slf4j
public class RecommendationContext {
 
    private RecommendationStrategy strategy;
 
    /**
     * Set the recommendation strategy at runtime.
     *
     * @param strategy the algorithm to use
     */
    public void setStrategy(RecommendationStrategy strategy) {
        log.info("Switching recommendation strategy to: {}", strategy.getStrategyName());
        this.strategy = strategy;
    }
 
    /**
     * Execute the current strategy.
     *
     * @param userId user to recommend for
     * @param limit  max number of results
     * @return list of recommended apps
     */
    public List<App> executeStrategy(Long userId, int limit) {
        if (strategy == null) {
            throw new IllegalStateException("No recommendation strategy set");
        }
        return strategy.recommend(userId, limit);
    }
 
    public String getCurrentStrategyName() {
        return strategy != null ? strategy.getStrategyName() : "NONE";
    }
}