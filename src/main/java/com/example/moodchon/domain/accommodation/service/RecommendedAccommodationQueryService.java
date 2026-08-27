package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.accommodation.dto.response.RecommendedAccommodationResponse;
import com.example.moodchon.domain.accommodation.repository.RecommendedAccommodationRepository;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendedAccommodationQueryService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final RecommendedAccommodationRepository recommendedAccommodationRepository;

    public List<RecommendedAccommodationResponse> getAll(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        return recommendedAccommodationRepository.findAllByChonkangIdOrderByRankAsc(chonkangId).stream()
                .map(RecommendedAccommodationResponse::of)
                .toList();
    }
}
