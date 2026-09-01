package com.example.moodchon.domain.save.service;

import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import com.example.moodchon.domain.save.dto.response.SaveFolderDetailResponse;
import com.example.moodchon.domain.save.dto.response.SaveFolderPlaceItemResponse;
import com.example.moodchon.domain.save.entity.SaveFolder;
import com.example.moodchon.domain.save.entity.SaveFolderPlace;
import com.example.moodchon.domain.save.repository.SaveFolderPlaceRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaveFolderPlaceQueryService {

    private final SaveFolderAccessValidator saveFolderAccessValidator;
    private final SaveFolderPlaceRepository saveFolderPlaceRepository;
    private final PostRepository postRepository;

    public SaveFolderDetailResponse getDetail(Long folderId, Long userId) {
        SaveFolder folder = saveFolderAccessValidator.validateOwner(folderId, userId);

        List<Place> places = saveFolderPlaceRepository.findAllBySaveFolderIdOrderByIdDesc(folderId).stream()
                .map(SaveFolderPlace::getPlace)
                .toList();

        Map<Long, List<Post>> postsByPlaceId = postsByPlaceId(places);

        List<SaveFolderPlaceItemResponse> items = places.stream()
                .map(place -> toItem(place, postsByPlaceId.getOrDefault(place.getId(), List.of())))
                .toList();

        return new SaveFolderDetailResponse(folder.getId(), folder.getName(), items);
    }

    private Map<Long, List<Post>> postsByPlaceId(List<Place> places) {
        List<Long> placeIds = places.stream().map(Place::getId).toList();
        return postRepository.findAllByPlaceIdIn(placeIds).stream()
                .collect(Collectors.groupingBy(post -> post.getPlace().getId()));
    }

    private SaveFolderPlaceItemResponse toItem(Place place, List<Post> posts) {
        String thumbnailImage = posts.stream()
                .findFirst()
                .map(Post::getImageUrl)
                .orElse(place.getThumbnailUrl());

        String representativeTag = posts.stream()
                .flatMap(post -> post.getTags().stream())
                .min(Comparator.comparingLong(MoodTag::getId))
                .map(MoodTag::getName)
                .orElse(null);

        return new SaveFolderPlaceItemResponse(
                place.getId(), place.getName(), place.getCategory(), thumbnailImage, representativeTag);
    }
}
