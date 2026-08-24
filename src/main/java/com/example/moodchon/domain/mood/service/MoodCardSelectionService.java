package com.example.moodchon.domain.mood.service;

import com.example.moodchon.domain.mood.dto.response.MoodCardResponse;
import com.example.moodchon.domain.mood.entity.AccommodationType;
import com.example.moodchon.domain.mood.entity.MoodCard;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.mood.repository.MoodCardRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 태그·숙소유형 그리디 커버리지 + 나머지 랜덤 채움. 커버리지 정확도는 시드 데이터에 달려있다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoodCardSelectionService {

    private static final int DISPLAY_COUNT = 16;

    private final MoodCardRepository moodCardRepository;

    public List<MoodCardResponse> selectRandomCards() {
        List<MoodCard> pool = new ArrayList<>(moodCardRepository.findAllWithTagsAndAccommodationType());
        Collections.shuffle(pool);

        if (pool.size() <= DISPLAY_COUNT) {
            return toResponses(pool);
        }

        LinkedHashSet<MoodCard> selected = new LinkedHashSet<>();
        Set<MoodTag> coveredTags = new LinkedHashSet<>();
        Set<AccommodationType> coveredTypes = new LinkedHashSet<>();

        for (MoodCard card : pool) {
            if (selected.size() >= DISPLAY_COUNT) {
                break;
            }
            if (!coveredTags.containsAll(card.getTags()) || !coveredTypes.contains(card.getAccommodationType())) {
                selected.add(card);
                coveredTags.addAll(card.getTags());
                coveredTypes.add(card.getAccommodationType());
            }
        }

        for (MoodCard card : pool) {
            if (selected.size() >= DISPLAY_COUNT) {
                break;
            }
            selected.add(card);
        }

        List<MoodCard> result = new ArrayList<>(selected);
        Collections.shuffle(result);
        return toResponses(result);
    }

    private List<MoodCardResponse> toResponses(List<MoodCard> cards) {
        return cards.stream().map(MoodCardResponse::from).toList();
    }
}
