package com.example.moodchon.domain.save.dto.response;

import com.example.moodchon.domain.place.entity.PlaceCategory;

public record SaveFolderPlaceItemResponse(
        Long placeId,
        String name,
        PlaceCategory category,
        String thumbnailImage,
        String representativeTag
) {
}
