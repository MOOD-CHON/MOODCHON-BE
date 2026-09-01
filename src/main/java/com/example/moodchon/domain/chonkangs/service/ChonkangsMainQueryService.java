package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.accommodation.dto.response.RecommendedAccommodationResponse;
import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.accommodation.repository.RecommendedAccommodationRepository;
import com.example.moodchon.domain.chonkangs.dto.response.ChonkangMainResponse;
import com.example.moodchon.domain.chonkangs.dto.response.ConfirmedAccommodationResponse;
import com.example.moodchon.domain.chonkangs.dto.response.MemberMoodProgressResponse;
import com.example.moodchon.domain.chonkangs.dto.response.MoodProgressResponse;
import com.example.moodchon.domain.chonkangs.dto.response.MoodResultResponse;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMainStatus;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMember;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import com.example.moodchon.domain.recommendation.dto.response.RecommendedItineraryResponse;
import com.example.moodchon.domain.recommendation.entity.RecommendedItinerary;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryItemRepository;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChonkangsMainQueryService {

    private static final int RECOMMENDED_ACCOMMODATION_PREVIEW_COUNT = 5;

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final ChonkangsRepository chonkangsRepository;
    private final ChonkangsMemberRepository chonkangsMemberRepository;
    private final ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;
    private final RecommendedAccommodationRepository recommendedAccommodationRepository;
    private final PostRepository postRepository;
    private final RecommendedItineraryRepository recommendedItineraryRepository;
    private final RecommendedItineraryItemRepository recommendedItineraryItemRepository;

    public ChonkangMainResponse getMain(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        ChonkangsMainStatus status = chonkang.resolveMainStatus();
        if (status == ChonkangsMainStatus.ACCOMMODATION_CONFIRMED) {
            return buildAccommodationConfirmed(chonkang);
        }
        if (status == ChonkangsMainStatus.MOOD_DECIDED) {
            return ChonkangMainResponse.moodDecided(MoodResultResponse.of(chonkang), buildRecommendedAccommodations(chonkangId));
        }

        return ChonkangMainResponse.moodVoting(buildMoodProgress(chonkangId));
    }

    private MoodProgressResponse buildMoodProgress(Long chonkangId) {
        List<ChonkangsMember> members = chonkangsMemberRepository.findAllWithUserByChonkangId(chonkangId);
        Set<Long> submittedUserIds = chonkangsMoodSelectionRepository.findAllByChonkangId(chonkangId).stream()
                .map(selection -> selection.getUser().getId())
                .collect(Collectors.toSet());

        List<MemberMoodProgressResponse> memberProgress = members.stream()
                .map(member -> MemberMoodProgressResponse.of(member, submittedUserIds.contains(member.getUser().getId())))
                .toList();

        return MoodProgressResponse.of(memberProgress);
    }

    private List<RecommendedAccommodationResponse> buildRecommendedAccommodations(Long chonkangId) {
        List<RecommendedAccommodation> recommendations = recommendedAccommodationRepository
                .findAllByChonkangIdOrderByRankAsc(chonkangId).stream()
                .limit(RECOMMENDED_ACCOMMODATION_PREVIEW_COUNT)
                .toList();

        List<Long> placeIds = recommendations.stream()
                .map(recommendation -> recommendation.getPlace().getId())
                .toList();
        Map<Long, List<String>> imagesByPlaceId = postRepository.findAllByPlaceIdIn(placeIds).stream()
                .collect(Collectors.groupingBy(
                        post -> post.getPlace().getId(),
                        Collectors.mapping(Post::getImageUrl, Collectors.toList())));

        return recommendations.stream()
                .map(recommendation -> RecommendedAccommodationResponse.of(
                        recommendation,
                        imagesByPlaceId.getOrDefault(recommendation.getPlace().getId(), List.of())))
                .toList();
    }

    private ChonkangMainResponse buildAccommodationConfirmed(Chonkangs chonkang) {
        ConfirmedAccommodationResponse confirmedAccommodation =
                ConfirmedAccommodationResponse.of(chonkang.getConfirmedAccommodation());

        RecommendedItineraryResponse itinerary = recommendedItineraryRepository.findByChonkangId(chonkang.getId())
                .filter(RecommendedItinerary::isCommitted)
                .map(this::toItineraryResponse)
                .orElse(null);

        return ChonkangMainResponse.accommodationConfirmed(MoodResultResponse.of(chonkang), confirmedAccommodation, itinerary);
    }

    private RecommendedItineraryResponse toItineraryResponse(RecommendedItinerary itinerary) {
        return RecommendedItineraryResponse.of(itinerary, recommendedItineraryItemRepository
                .findAllByRecommendedItineraryIdOrderByDayNumberAscOrderInDayAsc(itinerary.getId()));
    }
}
