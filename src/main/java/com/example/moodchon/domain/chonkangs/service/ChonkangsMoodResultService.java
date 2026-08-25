package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMoodSelection;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.mood.entity.MoodTag;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChonkangsMoodResultService {

    private final ChonkangsMemberRepository chonkangsMemberRepository;
    private final ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;

    public void confirmIfAllMembersSubmitted(Chonkangs chonkang) {
        List<ChonkangsMoodSelection> selections = chonkangsMoodSelectionRepository.findAllByChonkangId(chonkang.getId());
        long submittedMemberCount = selections.stream()
                .map(selection -> selection.getUser().getId())
                .distinct()
                .count();
        long totalMemberCount = chonkangsMemberRepository.countByChonkangId(chonkang.getId());

        if (submittedMemberCount < totalMemberCount) {
            return;
        }

        MoodTag topTag = resolveTopTag(selections);
        // TODO: 이름/설명 카피는 임시 문구. 태그 조합 -> 무드명 매핑표가 기획에서 확정되면 교체
        chonkang.confirmMood(topTag.getName(), buildDescription(topTag));
    }

    private MoodTag resolveTopTag(List<ChonkangsMoodSelection> selections) {
        Map<Long, MoodTag> tagsById = new LinkedHashMap<>();
        Map<Long, Long> tagCounts = new HashMap<>();

        for (ChonkangsMoodSelection selection : selections) {
            for (MoodTag tag : selection.getMoodCard().getTags()) {
                tagsById.putIfAbsent(tag.getId(), tag);
                tagCounts.merge(tag.getId(), 1L, Long::sum);
            }
        }

        Long topTagId = tagCounts.entrySet().stream()
                .max(Comparator.<Map.Entry<Long, Long>>comparingLong(Map.Entry::getValue)
                        .thenComparing(entry -> -entry.getKey()))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("무드 태그 집계 결과가 없습니다."));

        return tagsById.get(topTagId);
    }

    private String buildDescription(MoodTag topTag) {
        return "우리 팀이 가장 많이 선택한 무드는 '" + topTag.getName() + "'예요.";
    }
}
