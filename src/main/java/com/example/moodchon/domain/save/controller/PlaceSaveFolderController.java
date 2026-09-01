package com.example.moodchon.domain.save.controller;

import com.example.moodchon.domain.save.dto.request.UpdateSaveFolderSelectionRequest;
import com.example.moodchon.domain.save.dto.response.SaveFolderSelectionResponse;
import com.example.moodchon.domain.save.service.PlaceSaveFolderService;
import com.example.moodchon.global.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/places/{placeId}/save-folders")
@RequiredArgsConstructor
public class PlaceSaveFolderController {

    private final PlaceSaveFolderService placeSaveFolderService;

    @GetMapping
    public ApiResponse<List<SaveFolderSelectionResponse>> getSelection(
            @PathVariable Long placeId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(placeSaveFolderService.getSelection(userId, placeId));
    }

    @PutMapping
    public ApiResponse<Void> updateSelection(
            @PathVariable Long placeId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateSaveFolderSelectionRequest request) {
        placeSaveFolderService.updateSelection(userId, placeId, request);
        return ApiResponse.success();
    }
}
