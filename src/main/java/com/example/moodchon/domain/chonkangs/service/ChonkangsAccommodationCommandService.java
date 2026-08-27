package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.repository.PlaceRepository;
import com.example.moodchon.domain.recommendation.service.RecommendedItineraryResetService;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChonkangsAccommodationCommandService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final ChonkangsRepository chonkangsRepository;
    private final PlaceRepository placeRepository;
    private final RecommendedItineraryResetService recommendedItineraryResetService;

    public void confirm(Long chonkangId, Long userId, Long placeId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (place.getCategory() != PlaceCategory.ACCOMMODATION) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        recommendedItineraryResetService.resetFor(chonkangId);
        chonkang.confirmAccommodation(place);
    }

    public void cancel(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        chonkang.cancelAccommodation();
        recommendedItineraryResetService.resetFor(chonkangId);
    }
}
