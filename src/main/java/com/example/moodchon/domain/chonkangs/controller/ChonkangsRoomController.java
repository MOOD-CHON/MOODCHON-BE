package com.example.moodchon.domain.chonkangs.controller;

import com.example.moodchon.domain.chonkangs.dto.request.SubmitMoodSelectionRequest;
import com.example.moodchon.domain.chonkangs.dto.request.UpdateChonkangInfoRequest;
import com.example.moodchon.domain.chonkangs.dto.response.ChonkangInviteCodeResponse;
import com.example.moodchon.domain.chonkangs.dto.response.ChonkangMainResponse;
import com.example.moodchon.domain.chonkangs.dto.response.ChonkangMemberResponse;
import com.example.moodchon.domain.chonkangs.dto.response.LeaveChonkangResponse;
import com.example.moodchon.domain.chonkangs.dto.response.UpdateChonkangInfoResponse;
import com.example.moodchon.domain.chonkangs.service.ChonkangsInfoUpdateService;
import com.example.moodchon.domain.chonkangs.service.ChonkangsMainQueryService;
import com.example.moodchon.domain.chonkangs.service.ChonkangsMembershipService;
import com.example.moodchon.domain.chonkangs.service.ChonkangsMoodReminderService;
import com.example.moodchon.domain.chonkangs.service.ChonkangsMoodSelectionCommandService;
import com.example.moodchon.global.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chonkangs/{chonkangId}")
@RequiredArgsConstructor
public class ChonkangsRoomController {

    private final ChonkangsInfoUpdateService chonkangsInfoUpdateService;
    private final ChonkangsMoodSelectionCommandService chonkangsMoodSelectionCommandService;
    private final ChonkangsMembershipService chonkangsMembershipService;
    private final ChonkangsMainQueryService chonkangsMainQueryService;
    private final ChonkangsMoodReminderService chonkangsMoodReminderService;

    @GetMapping("/main")
    public ApiResponse<ChonkangMainResponse> getMain(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(chonkangsMainQueryService.getMain(chonkangId, userId));
    }

    @PatchMapping("/info")
    public ApiResponse<UpdateChonkangInfoResponse> updateInfo(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateChonkangInfoRequest request) {
        return ApiResponse.success(chonkangsInfoUpdateService.update(chonkangId, userId, request));
    }

    @PostMapping("/mood-selection")
    public ApiResponse<Void> submitMoodSelection(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SubmitMoodSelectionRequest request) {
        chonkangsMoodSelectionCommandService.submit(chonkangId, userId, request);
        return ApiResponse.success();
    }

    @PostMapping("/mood-selection/remind")
    public ApiResponse<Void> remindMoodSelection(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        chonkangsMoodReminderService.remind(chonkangId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/members")
    public ApiResponse<List<ChonkangMemberResponse>> getMembers(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(chonkangsMembershipService.getMembers(chonkangId, userId));
    }

    @GetMapping("/invite-code")
    public ApiResponse<ChonkangInviteCodeResponse> getInviteCode(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(chonkangsMembershipService.getInviteCode(chonkangId, userId));
    }

    @PostMapping("/leave")
    public ApiResponse<LeaveChonkangResponse> leave(
            @PathVariable Long chonkangId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(chonkangsMembershipService.leave(chonkangId, userId));
    }
}
