package com.appverse.pattern.strategy;
 
import com.appverse.entity.App;
import java.util.List;
 
/**
* Strategy Pattern — defines the contract for different
* recommendation algorithms.
*
* Concrete strategies:
* - TrendingRecommendationStrategy (by download count)
* - TopRatedRecommendationStrategy (by rating)
* - CategoryBasedRecommendationStrategy (by user preference)
*
* The strategy can be swapped at runtime without changing
* the RecommendationService client code.
*/
public interface RecommendationStrategy {
    List<App> recommend(Long userId, int limit);
    String getStrategyName();
}