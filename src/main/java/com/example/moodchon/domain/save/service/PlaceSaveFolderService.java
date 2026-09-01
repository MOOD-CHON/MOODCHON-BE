package com.example.moodchon.domain.save.service;

import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.repository.PlaceRepository;
import com.example.moodchon.domain.save.dto.request.UpdateSaveFolderSelectionRequest;
import com.example.moodchon.domain.save.dto.response.SaveFolderSelectionResponse;
import com.example.moodchon.domain.save.entity.SaveFolder;
import com.example.moodchon.domain.save.entity.SaveFolderPlace;
import com.example.moodchon.domain.save.repository.SaveFolderPlaceRepository;
import com.example.moodchon.domain.save.repository.SaveFolderRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceSaveFolderService {

    private final SaveFolderRepository saveFolderRepository;
    private final SaveFolderPlaceRepository saveFolderPlaceRepository;
    private final PlaceRepository placeRepository;

    public List<SaveFolderSelectionResponse> getSelection(Long userId, Long placeId) {
        List<SaveFolder> folders = saveFolderRepository.findAllByUserId(userId);
        Set<Long> savedFolderIds = savedFolderIds(userId, placeId);

        return folders.stream()
                .map(folder -> SaveFolderSelectionResponse.of(folder, savedFolderIds.contains(folder.getId())))
                .toList();
    }

    public void updateSelection(Long userId, Long placeId, UpdateSaveFolderSelectionRequest request) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        List<SaveFolder> myFolders = saveFolderRepository.findAllByUserId(userId);
        Map<Long, SaveFolder> myFoldersById = myFolders.stream()
                .collect(Collectors.toMap(SaveFolder::getId, Function.identity()));

        Set<Long> requestedFolderIds = new HashSet<>(request.folderIds());
        requestedFolderIds.retainAll(myFoldersById.keySet());

        List<SaveFolderPlace> existing = saveFolderPlaceRepository.findAllByPlaceIdAndSaveFolder_UserId(placeId, userId);
        Set<Long> existingFolderIds = existing.stream()
                .map(saveFolderPlace -> saveFolderPlace.getSaveFolder().getId())
                .collect(Collectors.toSet());

        List<SaveFolderPlace> toRemove = existing.stream()
                .filter(saveFolderPlace -> !requestedFolderIds.contains(saveFolderPlace.getSaveFolder().getId()))
                .toList();
        saveFolderPlaceRepository.deleteAll(toRemove);

        List<SaveFolderPlace> toAdd = requestedFolderIds.stream()
                .filter(folderId -> !existingFolderIds.contains(folderId))
                .map(folderId -> SaveFolderPlace.builder()
                        .saveFolder(myFoldersById.get(folderId))
                        .place(place)
                        .build())
                .toList();
        saveFolderPlaceRepository.saveAll(toAdd);
    }

    private Set<Long> savedFolderIds(Long userId, Long placeId) {
        return saveFolderPlaceRepository.findAllByPlaceIdAndSaveFolder_UserId(placeId, userId).stream()
                .map(saveFolderPlace -> saveFolderPlace.getSaveFolder().getId())
                .collect(Collectors.toSet());
    }
}
