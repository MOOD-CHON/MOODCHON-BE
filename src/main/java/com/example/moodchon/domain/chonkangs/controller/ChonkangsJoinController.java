package com.example.moodchon.domain.chonkangs.controller;

import com.example.moodchon.domain.chonkangs.dto.request.JoinChonkangRequest;
import com.example.moodchon.domain.chonkangs.dto.request.UpdateChonkangTripInfoRequest;
import com.example.moodchon.domain.chonkangs.dto.response.ChonkangTripInfoResponse;
import com.example.moodchon.domain.chonkangs.dto.response.JoinChonkangResponse;
import com.example.moodchon.domain.chonkangs.service.ChonkangsJoinService;
import com.example.moodchon.global.common.ApiResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/chonkangs/invite/{inviteCode}")
@RequiredArgsConstructor
public class ChonkangsJoinController {

    private final ChonkangsJoinService chonkangsJoinService;

    @GetMapping
    public ApiResponse<ChonkangTripInfoResponse> getTripInfo(@PathVariable String inviteCode) {
        return ApiResponse.success(chonkangsJoinService.getTripInfo(inviteCode));
    }

    @PatchMapping
    public ApiResponse<Void> updateTripInfo(
            @PathVariable String inviteCode,
            @Valid @RequestBody UpdateChonkangTripInfoRequest request) {
        chonkangsJoinService.updateTripInfo(inviteCode, request);
        return ApiResponse.success();
    }

    @PostMapping("/join")
    public ApiResponse<JoinChonkangResponse> join(
            @PathVariable String inviteCode,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody JoinChonkangRequest request) {
        return ApiResponse.success(chonkangsJoinService.join(inviteCode, userId, request));
    }
}
