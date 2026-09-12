package com.example.moodchon.domain.mood.service;

import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 사용자가 고른 무드 카드 id(= 게시물 id) 3개를 게시물로 바꿔준다.
@Component
@RequiredArgsConstructor
public class MoodCardResolver {

    private static final int REQUIRED_SELECTION_COUNT = 3;

    private final PostRepository postRepository;

    public List<Post> resolveExactlyThree(Set<Long> moodCardIds) {
        List<Post> posts = postRepository.findAllById(moodCardIds);
        if (posts.size() != REQUIRED_SELECTION_COUNT) {
            throw new CustomException(ErrorCode.INVALID_MOOD_SELECTION);
        }
        return posts;
    }
}
