package com.example.moodchon.domain.explore.service;

import com.example.moodchon.domain.explore.dto.response.ExplorePostResponse;
import com.example.moodchon.domain.explore.dto.response.MoodTagResponse;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.mood.repository.MoodTagRepository;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExploreQueryService {

    private final PostRepository postRepository;
    private final MoodTagRepository moodTagRepository;

    public List<MoodTagResponse> getMoodTags() {
        return moodTagRepository.findAll().stream()
                .map(MoodTagResponse::of)
                .toList();
    }

    public List<ExplorePostResponse> getFeed(Long tagId) {
        List<Post> posts = tagId != null
                ? postRepository.findAllByTagsIdOrderByCreatedAtDesc(tagId)
                : postRepository.findAllByOrderByCreatedAtDesc();

        return posts.stream()
                .map(post -> ExplorePostResponse.of(post, resolveRepresentativeTag(post, tagId)))
                .toList();
    }

    public List<ExplorePostResponse> search(String keyword) {
        return postRepository.searchByPlaceNameOrDescription(keyword).stream()
                .map(post -> ExplorePostResponse.of(post, resolveRepresentativeTag(post, null)))
                .toList();
    }

    private MoodTag resolveRepresentativeTag(Post post, Long tagId) {
        if (tagId != null) {
            return post.getTags().stream()
                    .filter(tag -> tag.getId().equals(tagId))
                    .findFirst()
                    .orElse(null);
        }

        return post.getTags().stream()
                .min(Comparator.comparingLong(MoodTag::getId))
                .orElse(null);
    }
}
