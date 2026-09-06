package com.example.moodchon.domain.accommodation.ai;

import com.example.moodchon.domain.accommodation.external.TourApiLodgingIntroFields;
import com.example.moodchon.domain.place.entity.Place;

public record AccommodationCandidate(Place place, TourApiLodgingIntroFields lodgingIntro) {
}
