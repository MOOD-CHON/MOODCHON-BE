package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.accommodation.ai.AccommodationMatchContext;
import com.example.moodchon.domain.accommodation.ai.AccommodationMatchResult;
import com.example.moodchon.domain.accommodation.ai.AccommodationMatcher;
import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.accommodation.external.TourApiClient;
import com.example.moodchon.domain.accommodation.external.TourApiPlace;
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

        AccommodationMatchResult result = accommodationMatcher.match(
                new AccommodationMatchContext(chonkang.getMoodName(), chonkang.getMoodDescription(), candidates));

        saveRanked(chonkang, candidates, result);
    }

    private void saveRanked(Chonkangs chonkang, List<Place> candidates, AccommodationMatchResult result) {
        Map<Long, Place> placeById = candidates.stream()
                .collect(Collectors.toMap(Place::getId, Function.identity()));

        List<AccommodationMatchResult.MatchedAccommodation> ranked = result.accommodations().stream()
                .filter(matched -> placeById.containsKey(matched.placeId()))
                .sorted(Comparator.comparingInt(AccommodationMatchResult.MatchedAccommodation::matchScore).reversed())
                .toList();

        List<RecommendedAccommodation> entities = new ArrayList<>();
        int rank = 0;
        for (AccommodationMatchResult.MatchedAccommodation matched : ranked) {
            entities.add(RecommendedAccommodation.builder()
                    .chonkang(chonkang)
                    .place(placeById.get(matched.placeId()))
                    .matchScore(clampScore(matched.matchScore()))
                    .rank(rank++)
                    .tags(matched.tags())
                    .highlights(matched.highlights())
                    .build());
        }

        recommendedAccommodationRepository.saveAll(entities);
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
