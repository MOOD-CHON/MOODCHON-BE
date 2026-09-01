package com.example.moodchon.domain.save.service;

import com.example.moodchon.domain.save.entity.SaveFolder;
import com.example.moodchon.domain.save.repository.SaveFolderRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveFolderAccessValidator {

    private final SaveFolderRepository saveFolderRepository;

    public SaveFolder validateOwner(Long folderId, Long userId) {
        SaveFolder folder = saveFolderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!folder.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return folder;
    }
}
