package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.accommodation.dto.response.RecommendedAccommodationResponse;
import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.accommodation.repository.RecommendedAccommodationRepository;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
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

    public List<RecommendedAccommodationResponse> getAll(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        List<RecommendedAccommodation> recommendations =
                recommendedAccommodationRepository.findAllByChonkangIdOrderByRankAsc(chonkangId);

        Map<Long, List<String>> imagesByPlaceId = imagesByPlaceId(recommendations);

        return recommendations.stream()
                .map(recommendation -> RecommendedAccommodationResponse.of(
                        recommendation,
                        imagesByPlaceId.getOrDefault(recommendation.getPlace().getId(), List.of())))
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
}
