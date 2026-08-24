package com.example.moodchon.domain.recommendation.controller;

import com.example.moodchon.domain.recommendation.dto.request.GenerateItineraryRequest;
import com.example.moodchon.domain.recommendation.dto.response.RecommendedItineraryResponse;
import com.example.moodchon.domain.recommendation.dto.response.RecommendedItinerarySummaryResponse;
import com.example.moodchon.domain.recommendation.service.RecommendedItineraryCommandService;
import com.example.moodchon.domain.recommendation.service.RecommendedItineraryGenerationService;
import com.example.moodchon.domain.recommendation.service.RecommendedItineraryQueryService;
import com.example.moodchon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chonkangs/{chonkangId}/recommended-itinerary")
@RequiredArgsConstructor
public class RecommendedItineraryController {

    private final RecommendedItineraryQueryService recommendedItineraryQueryService;
    private final RecommendedItineraryCommandService recommendedItineraryCommandService;
    private final RecommendedItineraryGenerationService recommendedItineraryGenerationService;

    @GetMapping
    public ApiResponse<RecommendedItineraryResponse> getDetail(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(recommendedItineraryQueryService.getDetail(chonkangId, userId));
    }

    @GetMapping("/summary")
    public ApiResponse<RecommendedItinerarySummaryResponse> getSummary(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(recommendedItineraryQueryService.getSummary(chonkangId, userId));
    }

    @PostMapping("/generate")
    public ApiResponse<Void> generate(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody GenerateItineraryRequest request) {
        recommendedItineraryGenerationService.generate(chonkangId, userId, request);
        return ApiResponse.success();
    }

    @PostMapping("/commit")
    public ApiResponse<Void> commit(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        recommendedItineraryCommandService.commit(chonkangId, userId);
        return ApiResponse.success();
    }
}
