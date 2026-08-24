package com.example.moodchon.domain.chonkangs.controller;

import com.example.moodchon.domain.chonkangs.dto.request.CreateChonkangRequest;
import com.example.moodchon.domain.chonkangs.dto.response.CreateChonkangResponse;
import com.example.moodchon.domain.chonkangs.service.ChonkangsCreateService;
import com.example.moodchon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chonkangs")
@RequiredArgsConstructor
public class ChonkangsCommandController {

    private final ChonkangsCreateService chonkangsCreateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateChonkangResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateChonkangRequest request) {
        return ApiResponse.success(chonkangsCreateService.create(userId, request));
    }
}
