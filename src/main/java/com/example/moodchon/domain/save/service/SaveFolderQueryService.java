package com.example.moodchon.domain.save.service;

import com.example.moodchon.domain.save.dto.response.SaveFolderResponse;
import com.example.moodchon.domain.save.repository.SaveFolderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaveFolderQueryService {

    private final SaveFolderRepository saveFolderRepository;

    public List<SaveFolderResponse> getMyFolders(Long userId) {
        return saveFolderRepository.findAllByUserId(userId).stream()
                .map(SaveFolderResponse::of)
                .toList();
    }
}
