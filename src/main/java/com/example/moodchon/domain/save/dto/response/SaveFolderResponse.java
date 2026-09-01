package com.example.moodchon.domain.save.dto.response;

import com.example.moodchon.domain.save.entity.SaveFolder;

public record SaveFolderResponse(
        Long id,
        String name
) {

    public static SaveFolderResponse of(SaveFolder folder) {
        return new SaveFolderResponse(folder.getId(), folder.getName());
    }
}
