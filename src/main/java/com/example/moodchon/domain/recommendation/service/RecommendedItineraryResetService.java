package com.example.moodchon.domain.recommendation.service;

import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryItemRepository;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendedItineraryResetService {

    private final RecommendedItineraryRepository recommendedItineraryRepository;
    private final RecommendedItineraryItemRepository recommendedItineraryItemRepository;

    public void resetFor(Long chonkangId) {
        recommendedItineraryRepository.findByChonkangId(chonkangId).ifPresent(itinerary -> {
            List<RecommendedItineraryItem> items = recommendedItineraryItemRepository
                    .findAllByRecommendedItineraryIdOrderByDayNumberAscOrderInDayAsc(itinerary.getId());
            recommendedItineraryItemRepository.deleteAll(items);
            recommendedItineraryRepository.delete(itinerary);
        });
    }
}
