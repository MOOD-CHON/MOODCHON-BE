package com.example.moodchon.domain.save.service;

import com.example.moodchon.domain.save.dto.request.CreateSaveFolderRequest;
import com.example.moodchon.domain.save.dto.response.SaveFolderResponse;
import com.example.moodchon.domain.save.entity.SaveFolder;
import com.example.moodchon.domain.save.repository.SaveFolderRepository;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.repository.UserRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SaveFolderCommandService {

    private final SaveFolderRepository saveFolderRepository;
    private final UserRepository userRepository;

    public SaveFolderResponse create(Long userId, CreateSaveFolderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        SaveFolder folder = saveFolderRepository.save(SaveFolder.builder()
                .user(user)
                .name(request.name())
                .build());

        return SaveFolderResponse.of(folder);
    }
}
