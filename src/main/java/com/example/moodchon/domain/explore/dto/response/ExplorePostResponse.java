package com.example.moodchon.domain.explore.dto.response;

import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.entity.PlaceCategory;

public record ExplorePostResponse(
        Long postId,
        String imageUrl,
        String representativeTag,
        Long placeId,
        PlaceCategory placeCategory
) {

    public static ExplorePostResponse of(Post post, MoodTag representativeTag) {
        return new ExplorePostResponse(
                post.getId(),
                post.getImageUrl(),
                representativeTag != null ? representativeTag.getName() : null,
                post.getPlace().getId(),
                post.getPlace().getCategory()
        );
    }
}
