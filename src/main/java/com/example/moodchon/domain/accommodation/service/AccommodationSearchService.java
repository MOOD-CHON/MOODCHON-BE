package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.accommodation.dto.response.AccommodationSearchResultResponse;
import com.example.moodchon.domain.accommodation.external.TourApiClient;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.service.PlaceContentSyncService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccommodationSearchService {

    private static final int SEARCH_RESULT_COUNT = 20;

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final TourApiClient tourApiClient;
    private final PlaceContentSyncService placeContentSyncService;

    public List<AccommodationSearchResultResponse> search(Long chonkangId, Long userId, String keyword) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        return tourApiClient.searchByKeyword(keyword, SEARCH_RESULT_COUNT).stream()
                .map(tourApiPlace -> placeContentSyncService.syncPlace(tourApiPlace, PlaceCategory.ACCOMMODATION))
                .map(AccommodationSearchResultResponse::from)
                .toList();
    }
}
