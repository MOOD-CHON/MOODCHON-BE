package com.example.moodchon.domain.recommendation.controller;

import com.example.moodchon.domain.recommendation.dto.request.AddCustomItineraryItemRequest;
import com.example.moodchon.domain.recommendation.dto.request.AddPlaceItineraryItemRequest;
import com.example.moodchon.domain.recommendation.dto.request.GenerateItineraryRequest;
import com.example.moodchon.domain.recommendation.dto.request.ReorderItineraryItemsRequest;
import com.example.moodchon.domain.recommendation.dto.response.ItineraryActivitySuggestionResponse;
import com.example.moodchon.domain.recommendation.dto.response.ItineraryItemDetailResponse;
import com.example.moodchon.domain.recommendation.dto.response.PlaceSearchResultResponse;
import com.example.moodchon.domain.recommendation.dto.response.RecommendedItineraryResponse;
import com.example.moodchon.domain.recommendation.dto.response.RecommendedItinerarySummaryResponse;
import com.example.moodchon.domain.recommendation.service.ItineraryEditCommandService;
import com.example.moodchon.domain.recommendation.service.ItineraryEditQueryService;
import com.example.moodchon.domain.recommendation.service.ItineraryItemDetailQueryService;
import com.example.moodchon.domain.recommendation.service.RecommendedItineraryCommandService;
import com.example.moodchon.domain.recommendation.service.RecommendedItineraryGenerationService;
import com.example.moodchon.domain.recommendation.service.RecommendedItineraryQueryService;
import com.example.moodchon.global.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chonkangs/{chonkangId}/recommended-itinerary")
@RequiredArgsConstructor
public class RecommendedItineraryController {

    private final RecommendedItineraryQueryService recommendedItineraryQueryService;
    private final RecommendedItineraryCommandService recommendedItineraryCommandService;
    private final RecommendedItineraryGenerationService recommendedItineraryGenerationService;
    private final ItineraryItemDetailQueryService itineraryItemDetailQueryService;
    private final ItineraryEditQueryService itineraryEditQueryService;
    private final ItineraryEditCommandService itineraryEditCommandService;

    @GetMapping
    public ApiResponse<RecommendedItineraryResponse> getDetail(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(recommendedItineraryQueryService.getDetail(chonkangId, userId));
    }

    @GetMapping("/items/{itemId}")
    public ApiResponse<ItineraryItemDetailResponse> getItemDetail(
            @PathVariable Long chonkangId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(itineraryItemDetailQueryService.getDetail(chonkangId, userId, itemId));
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

    @GetMapping("/suggestions")
    public ApiResponse<List<ItineraryActivitySuggestionResponse>> getSuggestions(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(itineraryEditQueryService.getSuggestions(chonkangId, userId));
    }

    @GetMapping("/places/search")
    public ApiResponse<List<PlaceSearchResultResponse>> searchPlaces(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(itineraryEditQueryService.searchPlaces(chonkangId, userId, keyword));
    }

    @GetMapping("/places/{placeId}/preview")
    public ApiResponse<ItineraryItemDetailResponse> previewPlace(
            @PathVariable Long chonkangId,
            @PathVariable Long placeId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(itineraryEditQueryService.previewPlace(chonkangId, userId, placeId));
    }

    @PostMapping("/days/{dayNumber}/items")
    public ApiResponse<Void> addPlaceItem(
            @PathVariable Long chonkangId,
            @PathVariable int dayNumber,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AddPlaceItineraryItemRequest request) {
        itineraryEditCommandService.addPlaceItem(chonkangId, userId, dayNumber, request.placeId());
        return ApiResponse.success();
    }

    @PostMapping("/days/{dayNumber}/items/custom")
    public ApiResponse<Void> addCustomItem(
            @PathVariable Long chonkangId,
            @PathVariable int dayNumber,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AddCustomItineraryItemRequest request) {
        itineraryEditCommandService.addCustomItem(chonkangId, userId, dayNumber, request.name(), request.category());
        return ApiResponse.success();
    }

    @PutMapping("/days/{dayNumber}/items/order")
    public ApiResponse<Void> reorderDay(
            @PathVariable Long chonkangId,
            @PathVariable int dayNumber,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReorderItineraryItemsRequest request) {
        itineraryEditCommandService.reorderDay(chonkangId, userId, dayNumber, request.itemIds());
        return ApiResponse.success();
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Void> deleteItem(
            @PathVariable Long chonkangId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal Long userId) {
        itineraryEditCommandService.deleteItem(chonkangId, userId, itemId);
        return ApiResponse.success();
    }
}
