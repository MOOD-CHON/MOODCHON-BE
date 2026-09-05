package com.example.moodchon.domain.recommendation.service;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.recommendation.entity.RecommendedItinerary;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;

// 9.x 일정 편집 화면 전용 공용 검증 — "확정된(committed) 일정만, 숙소가 확정된 촌캉스에서만" 편집/조회 가능하다.
final class ItineraryEditSupport {

    private ItineraryEditSupport() {
    }

    static RecommendedItinerary requireCommitted(RecommendedItineraryRepository repository, Long chonkangId) {
        RecommendedItinerary itinerary = repository.findByChonkangId(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        if (!itinerary.isCommitted()) {
            throw new CustomException(ErrorCode.ITINERARY_NOT_COMMITTED);
        }
        return itinerary;
    }

    static Place requireConfirmedAccommodation(Chonkangs chonkang) {
        Place accommodation = chonkang.getConfirmedAccommodation();
        if (accommodation == null) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }
        return accommodation;
    }
}
