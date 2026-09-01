package com.example.moodchon.domain.save.repository;

import com.example.moodchon.domain.save.entity.SaveFolderPlace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaveFolderPlaceRepository extends JpaRepository<SaveFolderPlace, Long> {

    boolean existsBySaveFolder_UserIdAndPlaceId(Long userId, Long placeId);

    List<SaveFolderPlace> findAllByPlaceIdAndSaveFolder_UserId(Long placeId, Long userId);
}
