package com.example.moodchon.domain.recommendation.service;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.place.GeoUtils;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.repository.PlaceRepository;
import com.example.moodchon.domain.recommendation.ai.ItineraryPlanContext;
import com.example.moodchon.domain.recommendation.ai.ItineraryPlanResult;
import com.example.moodchon.domain.recommendation.ai.ItineraryPlanner;
import com.example.moodchon.domain.recommendation.dto.request.GenerateItineraryRequest;
import com.example.moodchon.domain.recommendation.entity.RecommendedItinerary;
import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import com.example.moodchon.domain.recommendation.entity.TransportMode;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryItemRepository;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendedItineraryGenerationService {

    private static final double NEARBY_RADIUS_DEGREES = 0.15;
    private static final int MAX_CANDIDATE_PLACES = 60;
    private static final double WALK_THRESHOLD_KM = 1.0;
    private static final double WALK_SPEED_KMH = 4.0;
    private static final double CAR_SPEED_KMH = 30.0;

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final ChonkangsRepository chonkangsRepository;
    private final PlaceRepository placeRepository;
    private final RecommendedItineraryRepository recommendedItineraryRepository;
    private final RecommendedItineraryItemRepository recommendedItineraryItemRepository;
    private final ItineraryPlanner itineraryPlanner;

    public void generate(Long chonkangId, Long userId, GenerateItineraryRequest request) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        recommendedItineraryRepository.findByChonkangId(chonkangId)
                .ifPresent(this::deleteExistingDraft);

        Place accommodation = placeRepository.findById(request.accommodationPlaceId())
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        List<Place> candidates = placeRepository.findNearby(
                accommodation.getLatitude() - NEARBY_RADIUS_DEGREES,
                accommodation.getLatitude() + NEARBY_RADIUS_DEGREES,
                accommodation.getLongitude() - NEARBY_RADIUS_DEGREES,
                accommodation.getLongitude() + NEARBY_RADIUS_DEGREES,
                PageRequest.of(0, MAX_CANDIDATE_PLACES));

        int totalDays = (int) ChronoUnit.DAYS.between(chonkang.getStartDate(), chonkang.getEndDate()) + 1;

        ItineraryPlanResult plan = itineraryPlanner.plan(new ItineraryPlanContext(
                chonkang.getMoodName(), request.moodDescription(), totalDays, accommodation, candidates));

        saveItinerary(chonkang, accommodation, candidates, plan);
    }

    private void deleteExistingDraft(RecommendedItinerary existing) {
        if (existing.isCommitted()) {
            throw new CustomException(ErrorCode.ITINERARY_ALREADY_COMMITTED);
        }

        List<RecommendedItineraryItem> existingItems = recommendedItineraryItemRepository
                .findAllByRecommendedItineraryIdOrderByDayNumberAscOrderInDayAsc(existing.getId());
        recommendedItineraryItemRepository.deleteAll(existingItems);
        recommendedItineraryRepository.delete(existing);
        recommendedItineraryRepository.flush();
    }

    private void saveItinerary(Chonkangs chonkang, Place accommodation, List<Place> candidates,
                                ItineraryPlanResult plan) {
        RecommendedItinerary itinerary = recommendedItineraryRepository.save(
                RecommendedItinerary.builder().chonkang(chonkang).build());

        Map<Long, Place> placeById = candidates.stream()
                .collect(Collectors.toMap(Place::getId, Function.identity()));

        List<RecommendedItineraryItem> items = new ArrayList<>();
        for (ItineraryPlanResult.DayPlan day : plan.days()) {
            for (ItineraryPlanResult.ItemPlan planItem : day.items()) {
                Place place = placeById.get(planItem.placeId());
                if (place == null) {
                    continue;
                }
                items.add(toItineraryItem(itinerary, accommodation, place, day.dayNumber(), planItem));
            }
        }

        recommendedItineraryItemRepository.saveAll(items);
    }

    private RecommendedItineraryItem toItineraryItem(RecommendedItinerary itinerary, Place accommodation,
                                                       Place place, int dayNumber,
                                                       ItineraryPlanResult.ItemPlan planItem) {
        double distanceKm = GeoUtils.distanceKm(
                accommodation.getLatitude(), accommodation.getLongitude(),
                place.getLatitude(), place.getLongitude());

        TransportMode transportMode = distanceKm <= WALK_THRESHOLD_KM ? TransportMode.WALK : TransportMode.CAR;
        double speedKmh = transportMode == TransportMode.WALK ? WALK_SPEED_KMH : CAR_SPEED_KMH;
        int travelMinutes = (int) Math.max(1, Math.round(distanceKm / speedKmh * 60));

        return RecommendedItineraryItem.builder()
                .recommendedItinerary(itinerary)
                .place(place)
                .dayNumber(dayNumber)
                .orderInDay(planItem.order())
                .moodFitScore(clampScore(planItem.moodFitScore()))
                .aiSummary(planItem.aiSummary())
                .transportMode(transportMode)
                .travelMinutes(travelMinutes)
                .build();
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
