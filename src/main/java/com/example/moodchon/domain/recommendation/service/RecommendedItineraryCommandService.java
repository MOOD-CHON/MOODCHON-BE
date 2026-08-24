package com.example.moodchon.domain.recommendation.service;

import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.recommendation.entity.RecommendedItinerary;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendedItineraryCommandService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final RecommendedItineraryRepository recommendedItineraryRepository;

    public void commit(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        RecommendedItinerary itinerary = recommendedItineraryRepository.findByChonkangId(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        itinerary.commit();
    }
}
