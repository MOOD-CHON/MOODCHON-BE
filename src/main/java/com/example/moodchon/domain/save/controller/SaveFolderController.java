package com.example.moodchon.domain.save.controller;

import com.example.moodchon.domain.save.dto.request.CreateSaveFolderRequest;
import com.example.moodchon.domain.save.dto.response.SaveFolderResponse;
import com.example.moodchon.domain.save.service.SaveFolderCommandService;
import com.example.moodchon.domain.save.service.SaveFolderQueryService;
import com.example.moodchon.global.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/save-folders")
@RequiredArgsConstructor
public class SaveFolderController {

    private final SaveFolderQueryService saveFolderQueryService;
    private final SaveFolderCommandService saveFolderCommandService;

    @GetMapping
    public ApiResponse<List<SaveFolderResponse>> getMyFolders(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(saveFolderQueryService.getMyFolders(userId));
    }

    @PostMapping
    public ApiResponse<SaveFolderResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateSaveFolderRequest request) {
        return ApiResponse.success(saveFolderCommandService.create(userId, request));
    }
}
