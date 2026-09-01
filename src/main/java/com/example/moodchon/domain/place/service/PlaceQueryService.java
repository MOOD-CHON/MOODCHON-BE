package com.example.moodchon.domain.place.service;

import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.place.dto.response.PlaceDetailResponse;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PlaceRepository;
import com.example.moodchon.domain.place.repository.PostRepository;
import com.example.moodchon.domain.save.repository.SaveFolderPlaceRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceQueryService {

    private final PlaceRepository placeRepository;
    private final PostRepository postRepository;
    private final SaveFolderPlaceRepository saveFolderPlaceRepository;

    public PlaceDetailResponse getDetail(Long placeId, Long userId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        List<Post> posts = postRepository.findAllByPlaceIdIn(List.of(placeId));
        List<String> images = posts.stream()
                .map(Post::getImageUrl)
                .toList();
        List<String> tags = posts.stream()
                .flatMap(post -> post.getTags().stream())
                .map(MoodTag::getName)
                .distinct()
                .toList();

        boolean saved = saveFolderPlaceRepository.existsBySaveFolder_UserIdAndPlaceId(userId, placeId);

        return PlaceDetailResponse.of(place, images, tags, saved);
    }
}
