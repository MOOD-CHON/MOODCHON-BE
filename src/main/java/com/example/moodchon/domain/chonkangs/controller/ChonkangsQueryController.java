package com.example.moodchon.domain.chonkangs.controller;

import com.example.moodchon.domain.chonkangs.dto.ChonkangsFilter;
import com.example.moodchon.domain.chonkangs.dto.response.ChonkangHomeResponse;
import com.example.moodchon.domain.chonkangs.dto.response.ChonkangSummaryResponse;
import com.example.moodchon.domain.chonkangs.service.ChonkangsQueryService;
import com.example.moodchon.global.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chonkangs")
@RequiredArgsConstructor
public class ChonkangsQueryController {

    private final ChonkangsQueryService chonkangQueryService;

    @GetMapping("/me/home")
    public ApiResponse<ChonkangHomeResponse> getHome(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(chonkangQueryService.getHome(userId));
    }

    @GetMapping("/me")
    public ApiResponse<List<ChonkangSummaryResponse>> getMyChonkangs(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "ALL") ChonkangsFilter filter) {
        return ApiResponse.success(chonkangQueryService.getMyChonkangs(userId, filter));
    }
}
