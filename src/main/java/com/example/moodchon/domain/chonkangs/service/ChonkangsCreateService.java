package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.dto.request.CreateChonkangRequest;
import com.example.moodchon.domain.chonkangs.dto.response.CreateChonkangResponse;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMember;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMoodSelection;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.mood.entity.MoodCard;
import com.example.moodchon.domain.mood.repository.MoodCardRepository;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.repository.UserRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChonkangsCreateService {

    private static final long MAX_TRIP_SPAN_DAYS = 6;
    private static final int REQUIRED_MOOD_SELECTION_COUNT = 3;

    private final ChonkangsRepository chonkangsRepository;
    private final ChonkangsMemberRepository chonkangsMemberRepository;
    private final ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;
    private final MoodCardRepository moodCardRepository;
    private final UserRepository userRepository;
    private final InviteCodeGenerator inviteCodeGenerator;

    public CreateChonkangResponse create(Long hostId, CreateChonkangRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        List<MoodCard> selectedMoodCards = resolveMoodCards(request.selectedMoodCardIds());

        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Chonkangs chonkang = chonkangsRepository.save(Chonkangs.builder()
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .plannedMemberCount(request.plannedMemberCount())
                .companionType(request.companionType())
                .travelMethod(request.travelMethod())
                .desiredRegion(request.desiredRegion())
                .accommodationConditions(request.accommodationConditions())
                .inviteCode(inviteCodeGenerator.generate())
                .host(host)
                .build());

        chonkangsMemberRepository.save(ChonkangsMember.builder()
                .chonkang(chonkang)
                .user(host)
                .build());

        for (MoodCard moodCard : selectedMoodCards) {
            chonkangsMoodSelectionRepository.save(ChonkangsMoodSelection.builder()
                    .chonkang(chonkang)
                    .moodCard(moodCard)
                    .build());
        }

        return CreateChonkangResponse.from(chonkang);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }
        long spanDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (spanDays < 1 || spanDays > MAX_TRIP_SPAN_DAYS) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    private List<MoodCard> resolveMoodCards(Set<Long> selectedMoodCardIds) {
        List<MoodCard> moodCards = moodCardRepository.findAllById(selectedMoodCardIds);
        if (moodCards.size() != REQUIRED_MOOD_SELECTION_COUNT) {
            throw new CustomException(ErrorCode.INVALID_MOOD_SELECTION);
        }
        return moodCards;
    }
}
