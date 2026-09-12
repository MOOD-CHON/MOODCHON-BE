package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMoodSelection;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.mood.entity.MoodType;
import com.example.moodchon.domain.mood.repository.MoodTypeRepository;
import java.util.Comparator;
import java.util.HashMap;
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
    private final MoodTypeRepository moodTypeRepository;

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

        Map<Long, Long> tagCounts = countTagsByTagId(selections);
        MoodType moodType = resolveBestMoodType(tagCounts);
        chonkang.confirmMood(moodType.getName(), moodType.getDescription());
    }

    private Map<Long, Long> countTagsByTagId(List<ChonkangsMoodSelection> selections) {
        Map<Long, Long> tagCounts = new HashMap<>();
        for (ChonkangsMoodSelection selection : selections) {
            for (MoodTag tag : selection.getPost().getTags()) {
                tagCounts.merge(tag.getId(), 1L, Long::sum);
            }
        }
        return tagCounts;
    }

    private MoodType resolveBestMoodType(Map<Long, Long> tagCounts) {
        List<MoodType> moodTypes = moodTypeRepository.findAllWithCoreTags();

        return moodTypes.stream()
                .max(Comparator.<MoodType>comparingLong(moodType -> scoreOf(moodType, tagCounts))
                        .thenComparing(moodType -> -moodType.getId()))
                .orElseThrow(() -> new IllegalStateException("무드 유형 카탈로그가 비어 있습니다."));
    }

    private long scoreOf(MoodType moodType, Map<Long, Long> tagCounts) {
        return moodType.getCoreTags().stream()
                .mapToLong(tag -> tagCounts.getOrDefault(tag.getId(), 0L))
                .sum();
    }
}
