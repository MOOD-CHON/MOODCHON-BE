package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: FCM 등 실제 푸시 알림 연동 필요. 현재는 API 스펙만 제공하는 스텁.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChonkangsMoodReminderService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final ChonkangsRepository chonkangsRepository;

    public void remind(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }
}
