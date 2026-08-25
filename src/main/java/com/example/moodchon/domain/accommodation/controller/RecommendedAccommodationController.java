package com.example.moodchon.domain.accommodation.controller;

import com.example.moodchon.domain.accommodation.dto.response.RecommendedAccommodationResponse;
import com.example.moodchon.domain.accommodation.service.RecommendedAccommodationGenerationService;
import com.example.moodchon.domain.accommodation.service.RecommendedAccommodationQueryService;
import com.example.moodchon.global.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chonkangs/{chonkangId}/recommended-accommodations")
@RequiredArgsConstructor
public class RecommendedAccommodationController {

    private final RecommendedAccommodationGenerationService recommendedAccommodationGenerationService;
    private final RecommendedAccommodationQueryService recommendedAccommodationQueryService;

    @PostMapping("/generate")
    public ApiResponse<Void> generate(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        recommendedAccommodationGenerationService.generate(chonkangId, userId);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<List<RecommendedAccommodationResponse>> getAll(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(recommendedAccommodationQueryService.getAll(chonkangId, userId));
    }
}
