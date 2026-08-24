package com.example.moodchon.domain.recommendation.repository;

import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedItineraryItemRepository extends JpaRepository<RecommendedItineraryItem, Long> {

    List<RecommendedItineraryItem> findAllByRecommendedItineraryIdOrderByDayNumberAscOrderInDayAsc(
            Long recommendedItineraryId);
}
