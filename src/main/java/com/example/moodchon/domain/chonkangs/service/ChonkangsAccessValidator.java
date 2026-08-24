package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChonkangsAccessValidator {

    private final ChonkangsMemberRepository chonkangsMemberRepository;

    public void validateMember(Long chonkangId, Long userId) {
        if (!chonkangsMemberRepository.existsByChonkangIdAndUserId(chonkangId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}
