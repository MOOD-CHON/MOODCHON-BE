package com.example.moodchon.domain.save.dto.response;

import java.util.List;

public record SaveFolderDetailResponse(
        Long folderId,
        String name,
        List<SaveFolderPlaceItemResponse> places
) {
}
