package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.accommodation.dto.response.AccommodationVoterResponse;
import com.example.moodchon.domain.accommodation.dto.response.RecommendedAccommodationResponse;
import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.accommodation.repository.AccommodationVoteRepository;
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
import com.example.moodchon.domain.place.entity.Place;
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
    private final AccommodationVoteRepository accommodationVoteRepository;
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
            return ChonkangMainResponse.moodDecided(
                    chonkang.getName(), chonkang.getStartDate(), chonkang.getEndDate(),
                    MoodResultResponse.of(chonkang), buildRecommendedAccommodations(chonkangId, userId));
        }

        return ChonkangMainResponse.moodVoting(
                chonkang.getName(), chonkang.getStartDate(), chonkang.getEndDate(), buildMoodProgress(chonkangId));
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

    private List<RecommendedAccommodationResponse> buildRecommendedAccommodations(Long chonkangId, Long userId) {
        List<RecommendedAccommodation> recommendations = recommendedAccommodationRepository
                .findAllByChonkangIdOrderByRankAsc(chonkangId).stream()
                .limit(RECOMMENDED_ACCOMMODATION_PREVIEW_COUNT)
                .toList();

        Map<Long, List<String>> imagesByPlaceId = imagesByPlaceId(recommendations);
        Map<Long, List<AccommodationVoterResponse>> votersByRecommendedAccommodationId =
                votersByRecommendedAccommodationId(recommendations);

        return recommendations.stream()
                .map(recommendation -> toResponse(recommendation, userId, imagesByPlaceId, votersByRecommendedAccommodationId))
                .toList();
    }

    private Map<Long, List<AccommodationVoterResponse>> votersByRecommendedAccommodationId(
            List<RecommendedAccommodation> recommendations) {
        List<Long> recommendedAccommodationIds = recommendations.stream()
                .map(RecommendedAccommodation::getId)
                .toList();

        return accommodationVoteRepository.findAllWithUserByRecommendedAccommodationIdIn(recommendedAccommodationIds)
                .stream()
                .collect(Collectors.groupingBy(
                        vote -> vote.getRecommendedAccommodation().getId(),
                        Collectors.mapping(AccommodationVoterResponse::from, Collectors.toList())));
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
                                                          Map<Long, List<String>> imagesByPlaceId,
                                                          Map<Long, List<AccommodationVoterResponse>> votersByRecommendedAccommodationId) {
        List<String> images = imagesByPlaceId.getOrDefault(recommended.getPlace().getId(), List.of());
        long voteCount = accommodationVoteRepository.countByRecommendedAccommodationId(recommended.getId());
        boolean votedByMe = accommodationVoteRepository
                .existsByRecommendedAccommodationIdAndUserId(recommended.getId(), userId);
        List<AccommodationVoterResponse> voters =
                votersByRecommendedAccommodationId.getOrDefault(recommended.getId(), List.of());
        return RecommendedAccommodationResponse.of(recommended, images, voteCount, votedByMe, voters);
    }

    private ChonkangMainResponse buildAccommodationConfirmed(Chonkangs chonkang) {
        Place place = chonkang.getConfirmedAccommodation();
        // 추천을 거쳐 확정된 숙소면 AI가 만든 태그·좋은 점을 카드에 함께 보여준다.
        RecommendedAccommodation recommended = recommendedAccommodationRepository
                .findFirstByChonkangIdAndPlaceIdOrderByIdDesc(chonkang.getId(), place.getId())
                .orElse(null);
        ConfirmedAccommodationResponse confirmedAccommodation =
                ConfirmedAccommodationResponse.of(place, recommended);

        RecommendedItineraryResponse itinerary = recommendedItineraryRepository.findByChonkangId(chonkang.getId())
                .filter(RecommendedItinerary::isCommitted)
                .map(this::toItineraryResponse)
                .orElse(null);

        return ChonkangMainResponse.accommodationConfirmed(
                chonkang.getName(), chonkang.getStartDate(), chonkang.getEndDate(),
                MoodResultResponse.of(chonkang), confirmedAccommodation, itinerary);
    }

    private RecommendedItineraryResponse toItineraryResponse(RecommendedItinerary itinerary) {
        return RecommendedItineraryResponse.of(itinerary, recommendedItineraryItemRepository
                .findAllByRecommendedItineraryIdOrderByDayNumberAscOrderInDayAsc(itinerary.getId()));
    }
}
