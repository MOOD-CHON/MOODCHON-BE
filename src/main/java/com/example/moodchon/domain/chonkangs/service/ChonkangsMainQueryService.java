package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.dto.response.ChonkangMainResponse;
import com.example.moodchon.domain.chonkangs.dto.response.MemberMoodProgressResponse;
import com.example.moodchon.domain.chonkangs.dto.response.MoodProgressResponse;
import com.example.moodchon.domain.chonkangs.dto.response.MoodResultResponse;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMainStatus;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMember;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChonkangsMainQueryService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final ChonkangsRepository chonkangsRepository;
    private final ChonkangsMemberRepository chonkangsMemberRepository;
    private final ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;

    public ChonkangMainResponse getMain(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (chonkang.resolveMainStatus() == ChonkangsMainStatus.MOOD_DECIDED) {
            return ChonkangMainResponse.moodDecided(MoodResultResponse.of(chonkang));
        }

        return ChonkangMainResponse.moodVoting(buildMoodProgress(chonkangId));
    }

    private MoodProgressResponse buildMoodProgress(Long chonkangId) {
        List<ChonkangsMember> members = chonkangsMemberRepository.findAllWithUserByChonkangId(chonkangId);
        Set<Long> submittedUserIds = chonkangsMoodSelectionRepository.findAllByChonkangId(chonkangId).stream()
                .map(selection -> selection.getUser().getId())
                .collect(Collectors.toSet());

        List<MemberMoodProgressResponse> memberProgress = members.stream()
                .map(member -> MemberMoodProgressResponse.of(member, submittedUserIds.contains(member.getUser().getId())))
                .toList();

        return MoodProgressResponse.of(memberProgress);
    }
}
