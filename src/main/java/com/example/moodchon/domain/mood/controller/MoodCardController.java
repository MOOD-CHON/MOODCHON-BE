package com.example.moodchon.domain.mood.controller;

import com.example.moodchon.domain.mood.dto.response.MoodCardResponse;
import com.example.moodchon.domain.mood.service.MoodCardSelectionService;
import com.example.moodchon.global.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mood-cards")
@RequiredArgsConstructor
public class MoodCardController {

    private final MoodCardSelectionService moodCardSelectionService;

    @GetMapping("/random")
    public ApiResponse<List<MoodCardResponse>> getRandomCards() {
        return ApiResponse.success(moodCardSelectionService.selectRandomCards());
    }
}
