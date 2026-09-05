package com.example.moodchon.domain.accommodation.controller;

import com.example.moodchon.domain.accommodation.dto.response.AccommodationSearchResultResponse;
import com.example.moodchon.domain.accommodation.service.AccommodationSearchService;
import com.example.moodchon.global.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chonkangs/{chonkangId}/accommodation-search")
@RequiredArgsConstructor
public class AccommodationSearchController {

    private final AccommodationSearchService accommodationSearchService;

    @GetMapping
    public ApiResponse<List<AccommodationSearchResultResponse>> search(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(accommodationSearchService.search(chonkangId, userId, keyword));
    }
}
