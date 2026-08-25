package com.example.moodchon.domain.user.controller;

import com.example.moodchon.domain.user.dto.request.UpdateNicknameRequest;
import com.example.moodchon.domain.user.dto.request.UpdateNotificationPreferenceRequest;
import com.example.moodchon.domain.user.dto.request.UpdateProfileImageRequest;
import com.example.moodchon.domain.user.dto.response.UserProfileResponse;
import com.example.moodchon.domain.user.service.UserProfileService;
import com.example.moodchon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ApiResponse<UserProfileResponse> getMyProfile(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(userProfileService.getMyProfile(userId));
    }

    @PatchMapping("/nickname")
    public ApiResponse<UserProfileResponse> updateNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateNicknameRequest request) {
        return ApiResponse.success(userProfileService.updateNickname(userId, request));
    }

    @PatchMapping("/profile-image")
    public ApiResponse<UserProfileResponse> updateProfileImage(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateProfileImageRequest request) {
        return ApiResponse.success(userProfileService.updateProfileImage(userId, request));
    }

    @PatchMapping("/notification-preference")
    public ApiResponse<UserProfileResponse> updateNotificationPreference(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateNotificationPreferenceRequest request) {
        return ApiResponse.success(userProfileService.updateNotificationPreference(userId, request));
    }

    @DeleteMapping
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal Long userId) {
        userProfileService.withdraw(userId);
        return ApiResponse.success();
    }
}
