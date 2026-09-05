package com.example.moodchon.domain.chonkangs.controller;

import com.example.moodchon.domain.chonkangs.dto.response.ConfirmedAccommodationDetailResponse;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccommodationCommandService;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccommodationQueryService;
import com.example.moodchon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chonkangs/{chonkangId}/accommodation")
@RequiredArgsConstructor
public class ChonkangsAccommodationController {

    private final ChonkangsAccommodationCommandService chonkangsAccommodationCommandService;
    private final ChonkangsAccommodationQueryService chonkangsAccommodationQueryService;

    @GetMapping("/confirmed")
    public ApiResponse<ConfirmedAccommodationDetailResponse> getConfirmed(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(chonkangsAccommodationQueryService.getConfirmed(chonkangId, userId));
    }

    @PostMapping("/{placeId}/confirm")
    public ApiResponse<Void> confirm(
            @PathVariable Long chonkangId,
            @PathVariable Long placeId,
            @AuthenticationPrincipal Long userId) {
        chonkangsAccommodationCommandService.confirm(chonkangId, userId, placeId);
        return ApiResponse.success();
    }

    @DeleteMapping("/confirm")
    public ApiResponse<Void> cancel(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        chonkangsAccommodationCommandService.cancel(chonkangId, userId);
        return ApiResponse.success();
    }
}
