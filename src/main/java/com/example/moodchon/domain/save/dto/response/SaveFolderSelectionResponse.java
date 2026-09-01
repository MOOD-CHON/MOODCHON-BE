package com.example.moodchon.domain.save.dto.response;

import com.example.moodchon.domain.save.entity.SaveFolder;

public record SaveFolderSelectionResponse(
        Long folderId,
        String name,
        boolean saved
) {

    public static SaveFolderSelectionResponse of(SaveFolder folder, boolean saved) {
        return new SaveFolderSelectionResponse(folder.getId(), folder.getName(), saved);
    }
}
