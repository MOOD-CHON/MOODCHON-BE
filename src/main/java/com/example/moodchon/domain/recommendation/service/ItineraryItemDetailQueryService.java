package com.example.moodchon.domain.recommendation.service;

import com.example.moodchon.domain.accommodation.external.TourApiClient;
import com.example.moodchon.domain.accommodation.external.TourApiIntroFields;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import com.example.moodchon.domain.recommendation.dto.response.EventPlaceDetailInfo;
import com.example.moodchon.domain.recommendation.dto.response.GeneralPlaceDetailInfo;
import com.example.moodchon.domain.recommendation.dto.response.ItineraryItemDetailResponse;
import com.example.moodchon.domain.recommendation.dto.response.RestaurantPlaceDetailInfo;
import com.example.moodchon.domain.recommendation.dto.response.ShoppingPlaceDetailInfo;
import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryItemRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItineraryItemDetailQueryService {

    private static final Set<PlaceCategory> GENERAL_CATEGORIES =
            Set.of(PlaceCategory.TOURIST_SPOT, PlaceCategory.CULTURAL_FACILITY, PlaceCategory.LEISURE_SPORTS);
    private static final Set<PlaceCategory> EVENT_CATEGORIES =
            Set.of(PlaceCategory.EVENT, PlaceCategory.PERFORMANCE, PlaceCategory.FESTIVAL);

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final RecommendedItineraryItemRepository recommendedItineraryItemRepository;
    private final PostRepository postRepository;
    private final TourApiClient tourApiClient;

    public ItineraryItemDetailResponse getDetail(Long chonkangId, Long userId, Long itemId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        RecommendedItineraryItem item = recommendedItineraryItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        if (!item.getRecommendedItinerary().getChonkang().getId().equals(chonkangId)) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        Place place = item.getPlace();
        List<Post> posts = postRepository.findAllByPlaceIdIn(List.of(place.getId()));
        List<String> images = posts.stream().map(Post::getImageUrl).toList();
        List<String> tags = posts.stream()
                .flatMap(post -> post.getTags().stream())
                .map(MoodTag::getName)
                .distinct()
                .toList();

        PlaceCategory category = place.getCategory();
        TourApiIntroFields introFields = tourApiClient.fetchIntro(place.getExternalContentId(), category);

        GeneralPlaceDetailInfo generalInfo = GENERAL_CATEGORIES.contains(category)
                ? GeneralPlaceDetailInfo.of(introFields) : null;
        EventPlaceDetailInfo eventInfo = EVENT_CATEGORIES.contains(category)
                ? EventPlaceDetailInfo.of(introFields) : null;
        RestaurantPlaceDetailInfo restaurantInfo = category == PlaceCategory.RESTAURANT
                ? RestaurantPlaceDetailInfo.of(introFields) : null;
        ShoppingPlaceDetailInfo shoppingInfo = category == PlaceCategory.SHOPPING
                ? ShoppingPlaceDetailInfo.of(introFields) : null;

        return ItineraryItemDetailResponse.of(item, tags, images, generalInfo, eventInfo, restaurantInfo, shoppingInfo);
    }
}
