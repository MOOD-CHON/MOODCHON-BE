package com.example.moodchon.domain.place.service;

import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.mood.entity.MoodTagCategory;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 탐색 탭/장소 상세에서 사진(장소) 하나에 보여줄 "대표 무드 태그 한 개"를 정한다.
//
// AI 태깅은 장소마다 태그를 여러 개 붙이고 그 태그는 무드 결과/숙소 추천 계산에도 쓰이므로 저장된 태그는 건드리지 않는다.
// 대신 탐색 탭은 필터 칩(분위기 태그)과 같은 태그만 보여주되, 장소가 가진 분위기 태그 중 "가장 드문 것" 하나만 대표로 쓴다.
// 그러면 칩끼리 같은 사진이 겹치지 않고, 어떤 칩도 비지 않는다(가장 흔한 태그만 고르면 드문 태그 칩이 비어 버린다).
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepresentativeMoodTagResolver {

    private final PostRepository postRepository;

    // 게시물들이 속한 장소마다 대표 무드 태그를 정해 (placeId -> 태그)로 돌려준다. 분위기 태그가 없는 장소는 빠진다.
    // 같은 장소의 게시물은 모두 같은 태그를 갖도록, 장소에 달린 게시물 태그를 합쳐서 고른다.
    public Map<Long, MoodTag> representativeByPlace(List<Post> posts) {
        Map<Long, Long> placeCounts = countPlacesByAtmosphereTag();

        Map<Long, Set<MoodTag>> tagsByPlace = new HashMap<>();
        for (Post post : posts) {
            tagsByPlace.computeIfAbsent(post.getPlace().getId(), placeId -> new HashSet<>())
                    .addAll(post.getTags());
        }

        Map<Long, MoodTag> representativeByPlace = new HashMap<>();
        tagsByPlace.forEach((placeId, tags) ->
                pick(tags, placeCounts).ifPresent(tag -> representativeByPlace.put(placeId, tag)));
        return representativeByPlace;
    }

    // 한 장소의 태그들 중 대표 무드 태그.
    public Optional<MoodTag> representative(Collection<MoodTag> tags) {
        return pick(tags, countPlacesByAtmosphereTag());
    }

    // 분위기 태그 중 그 태그를 가진 장소가 가장 적은 것. 같으면 id가 작은 것.
    static Optional<MoodTag> pick(Collection<MoodTag> tags, Map<Long, Long> placeCounts) {
        return tags.stream()
                .filter(tag -> tag.getCategory() == MoodTagCategory.ATMOSPHERE)
                .min(Comparator
                        .comparingLong((MoodTag tag) -> placeCounts.getOrDefault(tag.getId(), 0L))
                        .thenComparingLong(MoodTag::getId));
    }

    private Map<Long, Long> countPlacesByAtmosphereTag() {
        Map<Long, Long> placeCounts = new HashMap<>();
        for (Object[] row : postRepository.countPlacesByAtmosphereTag()) {
            placeCounts.put((Long) row[0], (Long) row[1]);
        }
        return placeCounts;
    }
}
