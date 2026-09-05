package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.accommodation.dto.response.AccommodationVoterResponse;
import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.accommodation.repository.AccommodationVoteRepository;
import com.example.moodchon.domain.accommodation.repository.RecommendedAccommodationRepository;
import com.example.moodchon.domain.chonkangs.dto.response.ConfirmedAccommodationDetailResponse;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChonkangsAccommodationQueryService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final ChonkangsRepository chonkangsRepository;
    private final RecommendedAccommodationRepository recommendedAccommodationRepository;
    private final AccommodationVoteRepository accommodationVoteRepository;
    private final PostRepository postRepository;

    public ConfirmedAccommodationDetailResponse getConfirmed(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        Place place = chonkang.getConfirmedAccommodation();
        if (place == null) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        List<Post> posts = postRepository.findAllByPlaceIdIn(List.of(place.getId()));
        List<String> images = posts.stream().map(Post::getImageUrl).toList();
        List<String> tags = posts.stream()
                .flatMap(post -> post.getTags().stream())
                .map(MoodTag::getName)
                .distinct()
                .toList();

        Optional<RecommendedAccommodation> recommended = recommendedAccommodationRepository
                .findFirstByChonkangIdAndPlaceIdOrderByIdDesc(chonkangId, place.getId());

        Integer matchScore = recommended.map(RecommendedAccommodation::getMatchScore).orElse(null);
        Integer rank = recommended.map(RecommendedAccommodation::getRank).orElse(null);
        long voteCount = recommended
                .map(r -> accommodationVoteRepository.countByRecommendedAccommodationId(r.getId()))
                .orElse(0L);
        List<AccommodationVoterResponse> voters = recommended
                .map(r -> accommodationVoteRepository.findAllWithUserByRecommendedAccommodationId(r.getId()).stream()
                        .map(AccommodationVoterResponse::from)
                        .toList())
                .orElse(List.of());

        return ConfirmedAccommodationDetailResponse.of(place, images, tags, matchScore, rank, voteCount, voters);
    }
}
