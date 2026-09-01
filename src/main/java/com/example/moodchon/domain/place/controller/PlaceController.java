package com.example.moodchon.domain.place.controller;

import com.example.moodchon.domain.place.dto.response.PlaceDetailResponse;
import com.example.moodchon.domain.place.service.PlaceQueryService;
import com.example.moodchon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceQueryService placeQueryService;

    @GetMapping("/{placeId}")
    public ApiResponse<PlaceDetailResponse> getDetail(
            @PathVariable Long placeId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(placeQueryService.getDetail(placeId, userId));
    }
}
