package com.example.moodchon.domain.place.service;

import com.example.moodchon.domain.accommodation.external.TourApiClient;
import com.example.moodchon.domain.accommodation.external.TourApiPlace;
import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.mood.repository.MoodTagRepository;
import com.example.moodchon.domain.place.ai.PostTagContext;
import com.example.moodchon.domain.place.ai.PostTagResult;
import com.example.moodchon.domain.place.ai.PostTagger;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PlaceRepository;
import com.example.moodchon.domain.place.repository.PostRepository;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 탐색 탭 컨텐츠(장소+게시물)를 TourAPI로 동기화하고, 새로 생기는 게시물에는 장소 개요 설명 기반으로 AI가 무드 태그를 붙인다.
@Service
@RequiredArgsConstructor
@Transactional
public class PlaceContentSyncService {

    private static final int PLACE_SEARCH_COUNT = 20;

    // 관광사진갤러리는 관광지·레포츠는 매칭이 잘 되지만 숙소 상호명·소규모 행사명은 거의 안 걸려서
    // 이 두 카테고리만 검색한다 (하루 호출 한도를 불필요하게 쓰지 않기 위함).
    private static final Set<PlaceCategory> GALLERY_SEARCHABLE_CATEGORIES =
            EnumSet.of(PlaceCategory.TOURIST_SPOT, PlaceCategory.LEISURE_SPORTS);

    private final TourApiClient tourApiClient;
    private final PlaceRepository placeRepository;
    private final PostRepository postRepository;
    private final MoodTagRepository moodTagRepository;
    private final PostTagger postTagger;

    public void syncByRegion(Region region) {
        Map<String, MoodTag> tagsByName = tagsByName();

        for (PlaceCategory category : tourApiClient.syncablePlaceCategories()) {
            syncCategory(region, category, tagsByName);
        }
    }

    public Place syncPlace(TourApiPlace tourApiPlace, PlaceCategory category) {
        return syncPlace(tourApiPlace, category, tagsByName());
    }

    private void syncCategory(Region region, PlaceCategory category, Map<String, MoodTag> tagsByName) {
        List<TourApiPlace> tourApiPlaces = tourApiClient.searchPlaces(region, category, PLACE_SEARCH_COUNT);
        for (TourApiPlace tourApiPlace : tourApiPlaces) {
            syncPlace(tourApiPlace, category, tagsByName);
        }
    }

    private Place syncPlace(TourApiPlace tourApiPlace, PlaceCategory category, Map<String, MoodTag> tagsByName) {
        Place place = placeRepository.findByExternalContentId(tourApiPlace.contentId())
                .orElseGet(() -> placeRepository.save(Place.builder()
                        .externalContentId(tourApiPlace.contentId())
                        .name(tourApiPlace.name())
                        .category(category)
                        .address(tourApiPlace.address())
                        .latitude(tourApiPlace.latitude())
                        .longitude(tourApiPlace.longitude())
                        .thumbnailUrl(tourApiPlace.thumbnailUrl())
                        .build()));

        if (place.getDescription() == null) {
            place.updateDescription(tourApiClient.fetchOverview(tourApiPlace.contentId()));
        }

        if (place.getGalleryImageUrl() == null && GALLERY_SEARCHABLE_CATEGORIES.contains(category)) {
            place.updateGalleryImageUrl(tourApiClient.searchGalleryImage(place.getName()));
        }

        if (postRepository.existsByPlaceId(place.getId())) {
            return place;
        }

        Set<MoodTag> tags = resolveTags(place, tagsByName);
        List<Post> posts = tourApiClient.fetchImages(tourApiPlace.contentId()).stream()
                .map(imageUrl -> Post.builder().place(place).imageUrl(imageUrl).tags(tags).build())
                .toList();
        postRepository.saveAll(posts);
        return place;
    }

    private Set<MoodTag> resolveTags(Place place, Map<String, MoodTag> tagsByName) {
        PostTagResult result = postTagger.tag(new PostTagContext(
                place.getName(), place.getCategory(), place.getDescription(), List.copyOf(tagsByName.keySet())));

        return result.tagNames().stream()
                .map(tagsByName::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Map<String, MoodTag> tagsByName() {
        return moodTagRepository.findAll().stream()
                .collect(Collectors.toMap(MoodTag::getName, Function.identity()));
    }
}
