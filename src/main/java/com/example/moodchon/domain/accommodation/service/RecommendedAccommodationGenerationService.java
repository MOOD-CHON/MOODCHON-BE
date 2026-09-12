package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.accommodation.ai.AccommodationCandidate;
import com.example.moodchon.domain.accommodation.ai.AccommodationMatchContext;
import com.example.moodchon.domain.accommodation.ai.AccommodationMatchResult;
import com.example.moodchon.domain.accommodation.ai.AccommodationMatcher;
import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.accommodation.external.TourApiClient;
import com.example.moodchon.domain.accommodation.external.TourApiPlace;
import com.example.moodchon.domain.accommodation.external.TourApiLodgingIntroFields;
import com.example.moodchon.domain.accommodation.repository.AccommodationVoteRepository;
import com.example.moodchon.domain.accommodation.repository.RecommendedAccommodationRepository;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.service.PlaceContentSyncService;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendedAccommodationGenerationService {

    private static final int CANDIDATE_COUNT = 20;

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final ChonkangsRepository chonkangsRepository;
    private final TourApiClient tourApiClient;
    private final PlaceContentSyncService placeContentSyncService;
    private final AccommodationMatcher accommodationMatcher;
    private final RecommendedAccommodationRepository recommendedAccommodationRepository;
    private final AccommodationVoteRepository accommodationVoteRepository;

    public void generate(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!chonkang.isMoodDecided()) {
            throw new CustomException(ErrorCode.MOOD_NOT_DECIDED);
        }

        List<TourApiPlace> tourApiPlaces = tourApiClient.searchPlaces(
                chonkang.getDesiredRegion(), PlaceCategory.ACCOMMODATION, CANDIDATE_COUNT);
        List<Place> candidates = tourApiPlaces.stream()
                .map(tourApiPlace -> placeContentSyncService.syncPlace(tourApiPlace, PlaceCategory.ACCOMMODATION))
                .toList();

        List<Long> existingIds = recommendedAccommodationRepository.findAllByChonkangIdOrderByRankAsc(chonkangId)
                .stream()
                .map(RecommendedAccommodation::getId)
                .toList();
        accommodationVoteRepository.deleteAllByRecommendedAccommodationIdIn(existingIds);
        recommendedAccommodationRepository.deleteAllByChonkangId(chonkangId);
        recommendedAccommodationRepository.flush();

        if (candidates.isEmpty()) {
            return;
        }

        List<AccommodationCandidate> matchCandidates = candidates.stream()
                .map(place -> new AccommodationCandidate(
                        place, tourApiClient.fetchLodgingIntro(place.getExternalContentId())))
                .toList();

        AccommodationMatchResult result = accommodationMatcher.match(
                new AccommodationMatchContext(
                        chonkang.getMoodName(),
                        chonkang.getMoodDescription(),
                        chonkang.getPlannedMemberCount(),
                        chonkang.getAccommodationConditions(),
                        matchCandidates));

        saveRanked(chonkang, matchCandidates, result);
    }

    private void saveRanked(Chonkangs chonkang, List<AccommodationCandidate> matchCandidates,
                             AccommodationMatchResult result) {
        Map<Long, AccommodationCandidate> candidateByPlaceId = matchCandidates.stream()
                .collect(Collectors.toMap(candidate -> candidate.place().getId(), Function.identity()));

        List<AccommodationMatchResult.MatchedAccommodation> ranked = result.accommodations().stream()
                .filter(matched -> candidateByPlaceId.containsKey(matched.placeId()))
                .sorted(Comparator.comparingInt(AccommodationMatchResult.MatchedAccommodation::matchScore).reversed())
                .toList();

        List<RecommendedAccommodation> entities = new ArrayList<>();
        int rank = 0;
        for (AccommodationMatchResult.MatchedAccommodation matched : ranked) {
            AccommodationCandidate candidate = candidateByPlaceId.get(matched.placeId());
            TourApiLodgingIntroFields intro = candidate.lodgingIntro();
            entities.add(RecommendedAccommodation.builder()
                    .chonkang(chonkang)
                    .place(candidate.place())
                    .matchScore(clampScore(matched.matchScore()))
                    .rank(rank++)
                    .tags(matched.tags())
                    .highlights(matched.highlights())
                    .barbecueAvailable(parseFlag(intro.barbecue()))
                    .cookingAvailable(parseAvailability(intro.chkCooking()))
                    .petFriendly(parsePetFriendly(intro.petAccompanyType()))
                    .build());
        }

        recommendedAccommodationRepository.saveAll(entities);
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    // barbecue는 TourAPI가 "1"/"0"으로 내려주는 플래그 필드다.
    private Boolean parseFlag(String rawValue) {
        if ("1".equals(rawValue)) {
            return true;
        }
        if ("0".equals(rawValue)) {
            return false;
        }
        return null;
    }

    // chkCooking은 "가능"/"불가능"/빈값처럼 자유 텍스트로 내려온다.
    private Boolean parseAvailability(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue.contains("불가능")) {
            return false;
        }
        if (rawValue.contains("가능")) {
            return true;
        }
        return null;
    }

    // detailPetTour2는 등록된 숙소만 결과를 주기 때문에 값이 있으면 동반 가능으로 보고,
    // 없으면 "명시적으로 불가능"이 아니라 "정보없음"으로 다룬다.
    private Boolean parsePetFriendly(String petAccompanyType) {
        return (petAccompanyType == null || petAccompanyType.isBlank()) ? null : true;
    }
}
