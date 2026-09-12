package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.dto.request.JoinChonkangRequest;
import com.example.moodchon.domain.chonkangs.dto.request.UpdateChonkangTripInfoRequest;
import com.example.moodchon.domain.chonkangs.dto.response.ChonkangTripInfoResponse;
import com.example.moodchon.domain.chonkangs.dto.response.JoinChonkangResponse;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMember;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMoodSelection;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.mood.service.MoodCardResolver;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.recommendation.entity.RecommendedItinerary;
import com.example.moodchon.domain.recommendation.repository.RecommendedItineraryRepository;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.repository.UserRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChonkangsJoinService {

    private final ChonkangsRepository chonkangsRepository;
    private final ChonkangsMemberRepository chonkangsMemberRepository;
    private final ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;
    private final RecommendedItineraryRepository recommendedItineraryRepository;
    private final UserRepository userRepository;
    private final TripDateValidator tripDateValidator;
    private final MoodCardResolver moodCardResolver;
    private final ChonkangsMoodResultService chonkangsMoodResultService;

    @Transactional(readOnly = true)
    public ChonkangTripInfoResponse getTripInfo(String inviteCode) {
        Chonkangs chonkang = findByInviteCode(inviteCode);
        long currentMemberCount = chonkangsMemberRepository.countByChonkangId(chonkang.getId());

        if (currentMemberCount >= Chonkangs.MAX_MEMBER_COUNT) {
            throw new CustomException(ErrorCode.CHONKANG_FULL);
        }

        return ChonkangTripInfoResponse.of(chonkang, currentMemberCount);
    }

    public void updateTripInfo(String inviteCode, UpdateChonkangTripInfoRequest request) {
        tripDateValidator.validate(request.startDate(), request.endDate());
        Chonkangs chonkang = findByInviteCode(inviteCode);

        chonkang.updateTripInfo(
                request.name(), request.startDate(), request.endDate(), request.plannedMemberCount(),
                request.companionType(), request.travelMethod(), request.desiredRegion(),
                request.accommodationConditions());
    }

    public JoinChonkangResponse join(String inviteCode, Long userId, JoinChonkangRequest request) {
        Chonkangs chonkang = chonkangsRepository.findByInviteCodeForUpdate(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE_CODE));

        if (chonkangsMemberRepository.existsByChonkangIdAndUserId(chonkang.getId(), userId)) {
            throw new CustomException(ErrorCode.ALREADY_JOINED);
        }

        long currentMemberCount = chonkangsMemberRepository.countByChonkangId(chonkang.getId());
        if (currentMemberCount >= Chonkangs.MAX_MEMBER_COUNT) {
            throw new CustomException(ErrorCode.CHONKANG_FULL);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        boolean moodDecided = chonkang.isMoodDecided();

        chonkangsMemberRepository.save(ChonkangsMember.builder()
                .chonkang(chonkang)
                .user(user)
                .build());

        long updatedMemberCount = currentMemberCount + 1;
        boolean isLastParticipant = false;

        if (moodDecided) {
            // 4.1.3: 이미 무드가 결정된 방에 들어오는 참여자는 무드 검사를 다시 거치지 않는다.
        } else {
            Set<Long> selectedMoodCardIds = request.selectedMoodCardIds();
            if (selectedMoodCardIds == null) {
                throw new CustomException(ErrorCode.INVALID_MOOD_SELECTION);
            }
            List<Post> selectedPosts = moodCardResolver.resolveExactlyThree(selectedMoodCardIds);
            for (Post post : selectedPosts) {
                chonkangsMoodSelectionRepository.save(ChonkangsMoodSelection.builder()
                        .chonkang(chonkang)
                        .user(user)
                        .post(post)
                        .build());
            }
            chonkangsMoodResultService.confirmIfAllMembersSubmitted(chonkang);
            isLastParticipant = updatedMemberCount >= chonkang.getPlannedMemberCount();
        }

        RecommendedItinerary itinerary = recommendedItineraryRepository.findByChonkangId(chonkang.getId())
                .orElse(null);

        return new JoinChonkangResponse(
                chonkang.getId(),
                moodDecided,
                isLastParticipant,
                chonkang.getPlannedMemberCount(),
                updatedMemberCount,
                itinerary != null,
                itinerary != null && itinerary.isCommitted());
    }

    private Chonkangs findByInviteCode(String inviteCode) {
        return chonkangsRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE_CODE));
    }
}
