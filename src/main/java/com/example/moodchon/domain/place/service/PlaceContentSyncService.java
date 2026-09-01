package com.example.moodchon.domain.place.service;

import com.example.moodchon.domain.accommodation.external.TourApiClient;
import com.example.moodchon.domain.accommodation.external.TourApiPlace;
import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PlaceRepository;
import com.example.moodchon.domain.place.repository.PostRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 탐색 탭 컨텐츠(장소+게시물)를 TourAPI로 동기화한다. 무드 태그는 TourAPI에 없으므로 여기서 채우지 않는다.
@Service
@RequiredArgsConstructor
@Transactional
public class PlaceContentSyncService {

    private static final int PLACE_SEARCH_COUNT = 20;

    private final TourApiClient tourApiClient;
    private final PlaceRepository placeRepository;
    private final PostRepository postRepository;

    public void syncByRegion(Region region) {
        for (PlaceCategory category : tourApiClient.syncablePlaceCategories()) {
            syncCategory(region, category);
        }
    }

    private void syncCategory(Region region, PlaceCategory category) {
        List<TourApiPlace> tourApiPlaces = tourApiClient.searchPlaces(region, category, PLACE_SEARCH_COUNT);
        for (TourApiPlace tourApiPlace : tourApiPlaces) {
            syncPlace(tourApiPlace, category);
        }
    }

    public Place syncPlace(TourApiPlace tourApiPlace, PlaceCategory category) {
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

        if (postRepository.existsByPlaceId(place.getId())) {
            return place;
        }

        List<Post> posts = tourApiClient.fetchImages(tourApiPlace.contentId()).stream()
                .map(imageUrl -> Post.builder().place(place).imageUrl(imageUrl).build())
                .toList();
        postRepository.saveAll(posts);
        return place;
    }
}
