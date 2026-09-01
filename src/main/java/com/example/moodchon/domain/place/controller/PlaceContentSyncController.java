package com.example.moodchon.domain.place.controller;

import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.domain.place.service.PlaceContentSyncService;
import com.example.moodchon.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/explore/sync")
@RequiredArgsConstructor
public class PlaceContentSyncController {

    private final PlaceContentSyncService placeContentSyncService;

    @PostMapping
    public ApiResponse<Void> sync(
            @AuthenticationPrincipal Long userId,
            @RequestParam Region region) {
        placeContentSyncService.syncByRegion(region);
        return ApiResponse.success();
    }
}
