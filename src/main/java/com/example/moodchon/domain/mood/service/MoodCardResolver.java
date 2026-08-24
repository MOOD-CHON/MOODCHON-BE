package com.example.moodchon.domain.mood.service;

import com.example.moodchon.domain.mood.entity.MoodCard;
import com.example.moodchon.domain.mood.repository.MoodCardRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoodCardResolver {

    private static final int REQUIRED_SELECTION_COUNT = 3;

    private final MoodCardRepository moodCardRepository;

    public List<MoodCard> resolveExactlyThree(Set<Long> moodCardIds) {
        List<MoodCard> moodCards = moodCardRepository.findAllById(moodCardIds);
        if (moodCards.size() != REQUIRED_SELECTION_COUNT) {
            throw new CustomException(ErrorCode.INVALID_MOOD_SELECTION);
        }
        return moodCards;
    }
}
