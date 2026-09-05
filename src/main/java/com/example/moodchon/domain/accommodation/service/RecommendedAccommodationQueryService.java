package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.accommodation.dto.response.AccommodationVoterResponse;
import com.example.moodchon.domain.accommodation.dto.response.RecommendedAccommodationResponse;
import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.accommodation.repository.AccommodationVoteRepository;
import com.example.moodchon.domain.accommodation.repository.RecommendedAccommodationRepository;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendedAccommodationQueryService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final RecommendedAccommodationRepository recommendedAccommodationRepository;
    private final PostRepository postRepository;
    private final AccommodationVoteRepository accommodationVoteRepository;

    public List<RecommendedAccommodationResponse> getAll(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        List<RecommendedAccommodation> recommendations =
                recommendedAccommodationRepository.findAllByChonkangIdOrderByRankAsc(chonkangId);

        Map<Long, List<String>> imagesByPlaceId = imagesByPlaceId(recommendations);

        return recommendations.stream()
                .map(recommendation -> toResponse(recommendation, userId, imagesByPlaceId))
                .toList();
    }

    public RecommendedAccommodationResponse getOne(Long chonkangId, Long userId, Long placeId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        RecommendedAccommodation recommended = recommendedAccommodationRepository
                .findFirstByChonkangIdAndPlaceIdOrderByIdDesc(chonkangId, placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Map<Long, List<String>> imagesByPlaceId = imagesByPlaceId(List.of(recommended));
        return toResponse(recommended, userId, imagesByPlaceId);
    }

    public List<AccommodationVoterResponse> getVoters(Long chonkangId, Long userId, Long placeId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        RecommendedAccommodation recommended = recommendedAccommodationRepository
                .findFirstByChonkangIdAndPlaceIdOrderByIdDesc(chonkangId, placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        return accommodationVoteRepository.findAllWithUserByRecommendedAccommodationId(recommended.getId()).stream()
                .map(AccommodationVoterResponse::from)
                .toList();
    }

    private Map<Long, List<String>> imagesByPlaceId(List<RecommendedAccommodation> recommendations) {
        List<Long> placeIds = recommendations.stream()
                .map(recommendation -> recommendation.getPlace().getId())
                .toList();

        return postRepository.findAllByPlaceIdIn(placeIds).stream()
                .collect(Collectors.groupingBy(
                        post -> post.getPlace().getId(),
                        Collectors.mapping(Post::getImageUrl, Collectors.toList())));
    }

    private RecommendedAccommodationResponse toResponse(RecommendedAccommodation recommended, Long userId,
                                                          Map<Long, List<String>> imagesByPlaceId) {
        List<String> images = imagesByPlaceId.getOrDefault(recommended.getPlace().getId(), List.of());
        long voteCount = accommodationVoteRepository.countByRecommendedAccommodationId(recommended.getId());
        boolean votedByMe = accommodationVoteRepository
                .existsByRecommendedAccommodationIdAndUserId(recommended.getId(), userId);
        return RecommendedAccommodationResponse.of(recommended, images, voteCount, votedByMe);
    }
}
