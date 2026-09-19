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
                resolveImageUrl(post),
                post.getPlace().getName(),
                post.getPlace().getCategory(),
                post.getPlace().getCategory().getLabel(),
                post.getTags().stream().map(MoodTag::getName).toList()
        );
    }

    // 무드 카드는 안내판·실내 사진 같은 상세 이미지 대신, 관광사진갤러리 큐레이션 사진 → TourAPI
    // 대표 이미지 → 상세 이미지 순으로 우선순위를 둔다. 탐색 탭 게시물 자체(post.imageUrl)는
    // 그대로 두고 여기서 보여줄 값만 바꾼다.
    private static String resolveImageUrl(Post post) {
        String galleryImageUrl = post.getPlace().getGalleryImageUrl();
        if (galleryImageUrl != null && !galleryImageUrl.isBlank()) {
            return galleryImageUrl;
        }

        String thumbnailUrl = post.getPlace().getThumbnailUrl();
        return (thumbnailUrl == null || thumbnailUrl.isBlank()) ? post.getImageUrl() : thumbnailUrl;
    }
}
