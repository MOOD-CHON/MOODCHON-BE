package com.example.moodchon.domain.recommendation.service;

import com.example.moodchon.domain.accommodation.external.TourApiClient;
import com.example.moodchon.domain.accommodation.external.TourApiIntroFields;
import com.example.moodchon.domain.accommodation.external.TourApiPlaceSearchResult;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import com.example.moodchon.domain.place.service.PlaceContentSyncService;
import com.example.moodchon.domain.place.repository.PlaceRepository;
import com.example.moodchon.domain.recommendation.ai.ActivityScoreContext;
import com.example.moodchon.domain.recommendation.ai.ActivityScorePlanner;
import com.example.moodchon.domain.recommendation.ai.ActivityScoreResult;
import com.example.moodchon.domain.recommendation.dto.response.ItineraryActivitySuggestionResponse;
import com.example.moodchon.domain.recommendation.dto.response.ItineraryItemDetailResponse;
import com.example.moodchon.domain.recommendation.dto.response.PlaceSearchResultResponse;
import com.example.moodchon.domain.recommendation.entity.RecommendedItinerary;
import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryItemRepository;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItineraryEditQueryService {

    private static final double NEARBY_RADIUS_DEGREES = 0.15;
    private static final int CANDIDATE_POOL_SIZE = 30;
    private static final int SUGGESTION_COUNT = 8;
    private static final int SEARCH_RESULT_COUNT = 20;

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final RecommendedItineraryRepository recommendedItineraryRepository;
    private final RecommendedItineraryItemRepository recommendedItineraryItemRepository;
    private final PlaceRepository placeRepository;
    private final PostRepository postRepository;
    private final TourApiClient tourApiClient;
    private final PlaceContentSyncService placeContentSyncService;
    private final ActivityScorePlanner activityScorePlanner;

    public List<ItineraryActivitySuggestionResponse> getSuggestions(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);
        RecommendedItinerary itinerary = ItineraryEditSupport.requireCommitted(recommendedItineraryRepository, chonkangId);
        Chonkangs chonkang = itinerary.getChonkang();
        Place accommodation = ItineraryEditSupport.requireConfirmedAccommodation(chonkang);

        Set<Long> usedPlaceIds = usedPlaceIds(itinerary.getId());
        List<Place> candidates = placeRepository.findNearby(
                        accommodation.getLatitude() - NEARBY_RADIUS_DEGREES,
                        accommodation.getLatitude() + NEARBY_RADIUS_DEGREES,
                        accommodation.getLongitude() - NEARBY_RADIUS_DEGREES,
                        accommodation.getLongitude() + NEARBY_RADIUS_DEGREES,
                        PageRequest.of(0, CANDIDATE_POOL_SIZE))
                .stream()
                .filter(place -> !place.getId().equals(accommodation.getId()))
                .filter(place -> !usedPlaceIds.contains(place.getId()))
                .limit(SUGGESTION_COUNT)
                .toList();

        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<Long, ActivityScoreResult.ScoredPlace> scoreByPlaceId = score(chonkang, candidates);

        return candidates.stream()
                .filter(place -> scoreByPlaceId.containsKey(place.getId()))
                .map(place -> {
                    ActivityScoreResult.ScoredPlace scored = scoreByPlaceId.get(place.getId());
                    TravelEstimator.Estimate travel = TravelEstimator.from(
                            accommodation.getLatitude(), accommodation.getLongitude(),
                            place.getLatitude(), place.getLongitude());
                    return ItineraryActivitySuggestionResponse.of(place, scored.moodFitScore(), scored.aiSummary(),
                            travel.transportMode(), travel.travelMinutes());
                })
                .sorted(Comparator.comparingInt(ItineraryActivitySuggestionResponse::moodFitScore).reversed())
                .toList();
    }

    public List<PlaceSearchResultResponse> searchPlaces(Long chonkangId, Long userId, String keyword) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        return tourApiClient.searchAnyCategory(keyword, SEARCH_RESULT_COUNT).stream()
                .map(this::syncResult)
                .map(PlaceSearchResultResponse::from)
                .toList();
    }

    public ItineraryItemDetailResponse previewPlace(Long chonkangId, Long userId, Long placeId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);
        RecommendedItinerary itinerary = ItineraryEditSupport.requireCommitted(recommendedItineraryRepository, chonkangId);
        Chonkangs chonkang = itinerary.getChonkang();
        Place accommodation = ItineraryEditSupport.requireConfirmedAccommodation(chonkang);

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Map<Long, ActivityScoreResult.ScoredPlace> scoreByPlaceId = score(chonkang, List.of(place));
        ActivityScoreResult.ScoredPlace scored = scoreByPlaceId.get(place.getId());
        if (scored == null) {
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }

        TravelEstimator.Estimate travel = TravelEstimator.from(
                accommodation.getLatitude(), accommodation.getLongitude(),
                place.getLatitude(), place.getLongitude());

        List<Post> posts = postRepository.findAllByPlaceIdIn(List.of(placeId));
        List<String> images = posts.stream().map(Post::getImageUrl).toList();
        List<String> tags = posts.stream()
                .flatMap(post -> post.getTags().stream())
                .map(MoodTag::getName)
                .distinct()
                .toList();

        TourApiIntroFields introFields = tourApiClient.fetchIntro(place.getExternalContentId(), place.getCategory());
        PlaceCategoryDetailMapper.Blocks blocks = PlaceCategoryDetailMapper.map(place.getCategory(), introFields);

        return ItineraryItemDetailResponse.preview(place, scored.moodFitScore(), scored.aiSummary(),
                travel.transportMode(), travel.travelMinutes(), tags, images,
                blocks.generalInfo(), blocks.eventInfo(), blocks.restaurantInfo(), blocks.shoppingInfo());
    }

    private Place syncResult(TourApiPlaceSearchResult result) {
        return placeContentSyncService.syncPlace(result.place(), result.category());
    }

    private Map<Long, ActivityScoreResult.ScoredPlace> score(Chonkangs chonkang, List<Place> candidates) {
        ActivityScoreResult result = activityScorePlanner.score(
                new ActivityScoreContext(chonkang.getMoodName(), chonkang.getMoodDescription(), candidates));
        return result.items().stream()
                .collect(Collectors.toMap(ActivityScoreResult.ScoredPlace::placeId, Function.identity(),
                        (first, second) -> first));
    }

    private Set<Long> usedPlaceIds(Long recommendedItineraryId) {
        return recommendedItineraryItemRepository
                .findAllByRecommendedItineraryIdOrderByDayNumberAscOrderInDayAsc(recommendedItineraryId).stream()
                .filter(item -> !item.isCustom())
                .map(item -> item.getPlace().getId())
                .collect(Collectors.toSet());
    }

}
