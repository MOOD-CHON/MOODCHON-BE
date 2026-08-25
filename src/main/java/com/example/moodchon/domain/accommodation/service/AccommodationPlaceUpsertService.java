package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.accommodation.external.TourApiAccommodation;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccommodationPlaceUpsertService {

    private final PlaceRepository placeRepository;

    public Place upsert(TourApiAccommodation accommodation) {
        return placeRepository.findByExternalContentId(accommodation.contentId())
                .orElseGet(() -> placeRepository.save(Place.builder()
                        .externalContentId(accommodation.contentId())
                        .name(accommodation.name())
                        .category(PlaceCategory.ACCOMMODATION)
                        .address(accommodation.address())
                        .latitude(accommodation.latitude())
                        .longitude(accommodation.longitude())
                        .thumbnailUrl(accommodation.thumbnailUrl())
                        .build()));
    }
}
