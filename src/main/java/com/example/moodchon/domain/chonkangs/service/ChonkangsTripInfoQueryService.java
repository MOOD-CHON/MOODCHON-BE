package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.dto.response.ChonkangTripInfoResponse;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ChonkangTripInfoResponse는 원래 초대 참여 화면(초대코드 기준 조회)에서만 쓰였는데,
// 여행방 안에서도(전체 숙소 화면 등) 인원수/동행/이동방식/지역을 읽어야 해서 chonkangId 기준 조회를 추가한다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChonkangsTripInfoQueryService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final ChonkangsRepository chonkangsRepository;
    private final ChonkangsMemberRepository chonkangsMemberRepository;

    public ChonkangTripInfoResponse getTripInfo(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        long currentMemberCount = chonkangsMemberRepository.countByChonkangId(chonkangId);
        return ChonkangTripInfoResponse.of(chonkang, currentMemberCount);
    }
}
