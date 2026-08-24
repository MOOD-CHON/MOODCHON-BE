package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.dto.response.ChonkangInviteCodeResponse;
import com.example.moodchon.domain.chonkangs.dto.response.ChonkangMemberResponse;
import com.example.moodchon.domain.chonkangs.dto.response.LeaveChonkangResponse;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChonkangsMembershipService {

    private final ChonkangsRepository chonkangsRepository;
    private final ChonkangsMemberRepository chonkangsMemberRepository;
    private final ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;
    private final ChonkangsAccessValidator chonkangsAccessValidator;

    @Transactional(readOnly = true)
    public List<ChonkangMemberResponse> getMembers(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        return chonkangsMemberRepository.findAllWithUserByChonkangId(chonkangId).stream()
                .map(member -> ChonkangMemberResponse.of(member, chonkang.getHost().getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChonkangInviteCodeResponse getInviteCode(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        return new ChonkangInviteCodeResponse(chonkang.getInviteCode());
    }

    public LeaveChonkangResponse leave(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        chonkangsMoodSelectionRepository.deleteAllByChonkangIdAndUserId(chonkangId, userId);
        chonkangsMemberRepository.deleteByChonkangIdAndUserId(chonkangId, userId);

        LocalDate today = LocalDate.now();
        boolean hasOtherOngoingChonkang = chonkangsRepository.findAllByMemberUserId(userId).stream()
                .anyMatch(chonkang -> !chonkang.isCompleted(today));

        return new LeaveChonkangResponse(hasOtherOngoingChonkang);
    }
}
