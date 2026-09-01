package com.example.moodchon.domain.explore.controller;

import com.example.moodchon.domain.explore.dto.response.ExplorePostResponse;
import com.example.moodchon.domain.explore.dto.response.MoodTagResponse;
import com.example.moodchon.domain.explore.service.ExploreQueryService;
import com.example.moodchon.global.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/explore")
@RequiredArgsConstructor
public class ExploreController {

    private final ExploreQueryService exploreQueryService;

    @GetMapping("/mood-tags")
    public ApiResponse<List<MoodTagResponse>> getMoodTags() {
        return ApiResponse.success(exploreQueryService.getMoodTags());
    }

    @GetMapping("/posts")
    public ApiResponse<List<ExplorePostResponse>> getPosts(
            @RequestParam(required = false) Long tagId) {
        return ApiResponse.success(exploreQueryService.getFeed(tagId));
    }
}
