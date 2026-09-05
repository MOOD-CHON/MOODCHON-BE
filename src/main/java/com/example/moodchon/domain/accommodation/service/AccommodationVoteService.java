package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.accommodation.entity.AccommodationVote;
import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.accommodation.repository.AccommodationVoteRepository;
import com.example.moodchon.domain.accommodation.repository.RecommendedAccommodationRepository;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.repository.UserRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccommodationVoteService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final RecommendedAccommodationRepository recommendedAccommodationRepository;
    private final AccommodationVoteRepository accommodationVoteRepository;
    private final UserRepository userRepository;

    // 이미 투표한 경우 조용히 무시한다 — 비활성화된 버튼을 다시 눌러도 에러 없이 안전하게 처리하기 위함.
    public void vote(Long chonkangId, Long userId, Long placeId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        RecommendedAccommodation recommended = recommendedAccommodationRepository
                .findFirstByChonkangIdAndPlaceIdOrderByIdDesc(chonkangId, placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (accommodationVoteRepository.existsByRecommendedAccommodationIdAndUserId(recommended.getId(), userId)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        accommodationVoteRepository.save(AccommodationVote.builder()
                .recommendedAccommodation(recommended)
                .user(user)
                .build());
    }
}
