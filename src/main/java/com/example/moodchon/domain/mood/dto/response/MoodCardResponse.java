package com.example.moodchon.domain.mood.dto.response;

import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.entity.Post;
import java.util.List;

// 무드 카드는 탐색 탭 게시물(TourAPI 사진 + AI 태그)을 그대로 쓴다. id는 게시물 id다.
public record MoodCardResponse(
        Long id,
        String imageUrl,
        String placeName,
        PlaceCategory placeCategory,
        String placeCategoryLabel,
        List<String> tagNames
) {

    public static MoodCardResponse from(Post post) {
        return new MoodCardResponse(
                post.getId(),
                post.getImageUrl(),
                post.getPlace().getName(),
                post.getPlace().getCategory(),
                post.getPlace().getCategory().getLabel(),
                post.getTags().stream().map(MoodTag::getName).toList()
        );
    }
}
