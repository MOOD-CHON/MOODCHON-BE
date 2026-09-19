package com.example.moodchon.domain.explore.service;

import com.example.moodchon.domain.explore.dto.response.ExplorePostResponse;
import com.example.moodchon.domain.explore.dto.response.MoodTagResponse;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.mood.entity.MoodTagCategory;
import com.example.moodchon.domain.mood.repository.MoodTagRepository;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import com.example.moodchon.domain.place.service.RepresentativeMoodTagResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExploreQueryService {

    private final PostRepository postRepository;
    private final MoodTagRepository moodTagRepository;
    private final RepresentativeMoodTagResolver representativeMoodTagResolver;

    // 탐색 탭 필터 칩은 "분위기" 태그만 쓴다.
    public List<MoodTagResponse> getMoodTags() {
        return moodTagRepository.findAll().stream()
                .filter(tag -> tag.getCategory() == MoodTagCategory.ATMOSPHERE)
                .map(MoodTagResponse::of)
                .toList();
    }

    public List<ExplorePostResponse> getFeed(Long tagId) {
        return toResponses(postRepository.findAllByOrderByCreatedAtDesc(), tagId);
    }

    public List<ExplorePostResponse> search(String keyword) {
        return toResponses(postRepository.searchByPlaceNameOrDescription(keyword), null);
    }

    // 사진 한 장에는 대표 무드 태그 하나만 붙는다. tagId로 거르면 그 태그가 대표인 사진만 남으므로
    // 서로 다른 칩의 결과가 겹치지 않는다.
    private List<ExplorePostResponse> toResponses(List<Post> posts, Long tagId) {
        Map<Long, MoodTag> representativeByPlace = representativeMoodTagResolver.representativeByPlace(posts);

        List<ExplorePostResponse> responses = new ArrayList<>();
        for (Post post : posts) {
            MoodTag representative = representativeByPlace.get(post.getPlace().getId());

            if (tagId != null && (representative == null || !representative.getId().equals(tagId))) {
                continue;
            }
            responses.add(ExplorePostResponse.of(post, representative));
        }
        return responses;
    }
}
