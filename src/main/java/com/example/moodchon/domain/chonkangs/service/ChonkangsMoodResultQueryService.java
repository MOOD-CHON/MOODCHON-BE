package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.dto.response.MoodResultDetailResponse;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMoodSelection;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 태그 빈도 요약 문장은 간단한 템플릿으로 생성 — 마케팅 문구 수준의 자연스러움이 필요하면 추후 AI 생성으로 교체 고려.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChonkangsMoodResultQueryService {

    private static final int COLLAGE_IMAGE_COUNT = 4;
    private static final int TAG_BREAKDOWN_LIMIT = 4;

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final ChonkangsRepository chonkangsRepository;
    private final ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;

    public MoodResultDetailResponse getDetail(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!chonkang.isMoodDecided()) {
            throw new CustomException(ErrorCode.MOOD_NOT_DECIDED);
        }

        List<ChonkangsMoodSelection> selections = chonkangsMoodSelectionRepository.findAllByChonkangId(chonkangId);

        List<String> collageImageUrls = selections.stream()
                .map(selection -> selection.getMoodCard().getImageUrl())
                .distinct()
                .limit(COLLAGE_IMAGE_COUNT)
                .toList();

        List<MoodResultDetailResponse.TagFrequency> tagBreakdown = buildTagBreakdown(selections);

        return new MoodResultDetailResponse(
                chonkang.getMoodName(), chonkang.getMoodDescription(), collageImageUrls, tagBreakdown,
                buildSummary(tagBreakdown));
    }

    private List<MoodResultDetailResponse.TagFrequency> buildTagBreakdown(List<ChonkangsMoodSelection> selections) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ChonkangsMoodSelection selection : selections) {
            for (MoodTag tag : selection.getMoodCard().getTags()) {
                counts.merge(tag.getName(), 1L, Long::sum);
            }
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TAG_BREAKDOWN_LIMIT)
                .map(entry -> new MoodResultDetailResponse.TagFrequency(entry.getKey(), entry.getValue()))
                .toList();
    }

    private String buildSummary(List<MoodResultDetailResponse.TagFrequency> tagBreakdown) {
        if (tagBreakdown.size() >= 2) {
            return "구성원들의 선택에서 " + tagBreakdown.get(0).tagName() + "과 " + tagBreakdown.get(1).tagName()
                    + " 취향이 많이 겹쳤어요.";
        }
        if (tagBreakdown.size() == 1) {
            return "구성원들의 선택에서 " + tagBreakdown.get(0).tagName() + " 취향이 두드러졌어요.";
        }
        return "";
    }
}
