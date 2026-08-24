package com.example.moodchon.domain.recommendation.service;

import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.recommendation.dto.response.RecommendedItineraryResponse;
import com.example.moodchon.domain.recommendation.dto.response.RecommendedItinerarySummaryResponse;
import com.example.moodchon.domain.recommendation.entity.RecommendedItinerary;
import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryItemRepository;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendedItineraryQueryService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final RecommendedItineraryRepository recommendedItineraryRepository;
    private final RecommendedItineraryItemRepository recommendedItineraryItemRepository;

    public RecommendedItineraryResponse getDetail(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        RecommendedItinerary itinerary = findByChonkangId(chonkangId);
        List<RecommendedItineraryItem> items = recommendedItineraryItemRepository
                .findAllByRecommendedItineraryIdOrderByDayNumberAscOrderInDayAsc(itinerary.getId());

        return RecommendedItineraryResponse.of(itinerary, items);
    }

    public RecommendedItinerarySummaryResponse getSummary(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        return recommendedItineraryRepository.findByChonkangId(chonkangId)
                .map(RecommendedItinerarySummaryResponse::of)
                .orElseGet(RecommendedItinerarySummaryResponse::none);
    }

    private RecommendedItinerary findByChonkangId(Long chonkangId) {
        return recommendedItineraryRepository.findByChonkangId(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }
}
