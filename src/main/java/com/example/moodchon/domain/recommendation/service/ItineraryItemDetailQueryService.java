package com.example.moodchon.domain.recommendation.service;

import com.example.moodchon.domain.accommodation.external.TourApiClient;
import com.example.moodchon.domain.accommodation.external.TourApiIntroFields;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import com.example.moodchon.domain.recommendation.dto.response.ItineraryItemDetailResponse;
import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryItemRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItineraryItemDetailQueryService {

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
        PlaceCategoryDetailMapper.Blocks blocks = PlaceCategoryDetailMapper.map(category, introFields);

        return ItineraryItemDetailResponse.of(item, tags, images, blocks.generalInfo(), blocks.eventInfo(),
                blocks.restaurantInfo(), blocks.shoppingInfo());
    }
}
