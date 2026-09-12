package com.example.moodchon.domain.mood.service;

import com.example.moodchon.domain.mood.dto.response.MoodCardResponse;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 무드 카드로 보여줄 게시물 16장을 고른다. 태그·장소 카테고리를 골고루 덮도록 그리디로 뽑고,
// 같은 장소의 사진이 여러 장 겹치지 않게 장소당 1장까지만 넣는다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoodCardSelectionService {

    private static final int DISPLAY_COUNT = 16;

    private final PostRepository postRepository;

    public List<MoodCardResponse> selectRandomCards() {
        List<Post> pool = new ArrayList<>(postRepository.findAllWithPlaceAndTags());
        Collections.shuffle(pool);

        LinkedHashSet<Post> selected = new LinkedHashSet<>();
        Set<Long> usedPlaceIds = new HashSet<>();
        Set<MoodTag> coveredTags = new LinkedHashSet<>();
        Set<PlaceCategory> coveredCategories = new LinkedHashSet<>();

        // 1차: 아직 안 나온 태그나 카테고리를 새로 덮는 게시물부터 채운다.
        for (Post post : pool) {
            if (selected.size() >= DISPLAY_COUNT) {
                break;
            }
            if (usedPlaceIds.contains(post.getPlace().getId())) {
                continue;
            }
            boolean widensCoverage = !coveredTags.containsAll(post.getTags())
                    || !coveredCategories.contains(post.getPlace().getCategory());
            if (!widensCoverage) {
                continue;
            }
            select(post, selected, usedPlaceIds, coveredTags, coveredCategories);
        }

        // 2차: 남은 자리를 장소 중복 없이 랜덤으로 채운다.
        for (Post post : pool) {
            if (selected.size() >= DISPLAY_COUNT) {
                break;
            }
            if (usedPlaceIds.contains(post.getPlace().getId())) {
                continue;
            }
            select(post, selected, usedPlaceIds, coveredTags, coveredCategories);
        }

        // 3차: 장소가 16곳보다 적을 때만 같은 장소의 다른 사진을 허용한다.
        for (Post post : pool) {
            if (selected.size() >= DISPLAY_COUNT) {
                break;
            }
            selected.add(post);
        }

        List<Post> result = new ArrayList<>(selected);
        Collections.shuffle(result);
        return result.stream().map(MoodCardResponse::from).toList();
    }

    private void select(Post post, Set<Post> selected, Set<Long> usedPlaceIds,
                        Set<MoodTag> coveredTags, Set<PlaceCategory> coveredCategories) {
        selected.add(post);
        usedPlaceIds.add(post.getPlace().getId());
        coveredTags.addAll(post.getTags());
        coveredCategories.add(post.getPlace().getCategory());
    }
}
