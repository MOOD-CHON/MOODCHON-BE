package com.example.moodchon.domain.recommendation.service;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.repository.PlaceRepository;
import com.example.moodchon.domain.recommendation.ai.ActivityScoreContext;
import com.example.moodchon.domain.recommendation.ai.ActivityScorePlanner;
import com.example.moodchon.domain.recommendation.ai.ActivityScoreResult;
import com.example.moodchon.domain.recommendation.entity.RecommendedItinerary;
import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryItemRepository;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItineraryEditCommandService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final RecommendedItineraryRepository recommendedItineraryRepository;
    private final RecommendedItineraryItemRepository recommendedItineraryItemRepository;
    private final PlaceRepository placeRepository;
    private final ActivityScorePlanner activityScorePlanner;

    public void addPlaceItem(Long chonkangId, Long userId, int dayNumber, Long placeId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);
        RecommendedItinerary itinerary = ItineraryEditSupport.requireCommitted(recommendedItineraryRepository, chonkangId);
        Chonkangs chonkang = itinerary.getChonkang();
        validateDayNumber(chonkang, dayNumber);
        Place accommodation = ItineraryEditSupport.requireConfirmedAccommodation(chonkang);

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        ActivityScoreResult result = activityScorePlanner.score(
                new ActivityScoreContext(chonkang.getMoodName(), chonkang.getMoodDescription(), List.of(place)));
        ActivityScoreResult.ScoredPlace scored = result.items().stream()
                .filter(item -> item.placeId() == place.getId())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.AI_GENERATION_FAILED));

        TravelEstimator.Estimate travel = TravelEstimator.from(
                accommodation.getLatitude(), accommodation.getLongitude(),
                place.getLatitude(), place.getLongitude());

        recommendedItineraryItemRepository.save(RecommendedItineraryItem.builder()
                .recommendedItinerary(itinerary)
                .place(place)
                .dayNumber(dayNumber)
                .orderInDay(nextOrder(itinerary.getId(), dayNumber))
                .moodFitScore(clampScore(scored.moodFitScore()))
                .aiSummary(scored.aiSummary())
                .transportMode(travel.transportMode())
                .travelMinutes(travel.travelMinutes())
                .build());
    }

    public void addCustomItem(Long chonkangId, Long userId, int dayNumber, String name, PlaceCategory category) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);
        RecommendedItinerary itinerary = ItineraryEditSupport.requireCommitted(recommendedItineraryRepository, chonkangId);
        validateDayNumber(itinerary.getChonkang(), dayNumber);

        recommendedItineraryItemRepository.save(RecommendedItineraryItem.builder()
                .recommendedItinerary(itinerary)
                .customName(name)
                .customCategory(category)
                .dayNumber(dayNumber)
                .orderInDay(nextOrder(itinerary.getId(), dayNumber))
                .moodFitScore(0)
                .build());
    }

    public void reorderDay(Long chonkangId, Long userId, int dayNumber, List<Long> itemIds) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);
        RecommendedItinerary itinerary = ItineraryEditSupport.requireCommitted(recommendedItineraryRepository, chonkangId);

        List<RecommendedItineraryItem> existing = recommendedItineraryItemRepository
                .findAllByRecommendedItineraryIdAndDayNumberOrderByOrderInDayAsc(itinerary.getId(), dayNumber);

        Map<Long, RecommendedItineraryItem> itemsById = existing.stream()
                .collect(Collectors.toMap(RecommendedItineraryItem::getId, Function.identity()));

        if (itemsById.size() != itemIds.size() || !itemsById.keySet().containsAll(itemIds)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        for (int i = 0; i < itemIds.size(); i++) {
            itemsById.get(itemIds.get(i)).changeOrder(i + 1);
        }
    }

    public void deleteItem(Long chonkangId, Long userId, Long itemId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);
        ItineraryEditSupport.requireCommitted(recommendedItineraryRepository, chonkangId);

        RecommendedItineraryItem item = recommendedItineraryItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        if (!item.getRecommendedItinerary().getChonkang().getId().equals(chonkangId)) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        recommendedItineraryItemRepository.delete(item);
    }

    private int nextOrder(Long recommendedItineraryId, int dayNumber) {
        return recommendedItineraryItemRepository
                .findAllByRecommendedItineraryIdAndDayNumberOrderByOrderInDayAsc(recommendedItineraryId, dayNumber)
                .stream()
                .mapToInt(RecommendedItineraryItem::getOrderInDay)
                .max()
                .orElse(0) + 1;
    }

    private void validateDayNumber(Chonkangs chonkang, int dayNumber) {
        int totalDays = (int) ChronoUnit.DAYS.between(chonkang.getStartDate(), chonkang.getEndDate()) + 1;
        if (dayNumber < 1 || dayNumber > totalDays) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
