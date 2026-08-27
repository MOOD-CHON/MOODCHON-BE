package com.example.moodchon.domain.chonkangs.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.moodchon.domain.chonkangs.dto.request.SubmitMoodSelectionRequest;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.CompanionType;
import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.domain.chonkangs.entity.TravelMethod;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.mood.entity.AccommodationType;
import com.example.moodchon.domain.mood.entity.MoodCard;
import com.example.moodchon.domain.mood.service.MoodCardResolver;
import com.example.moodchon.domain.user.entity.AuthProvider;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.entity.UserRole;
import com.example.moodchon.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChonkangsMoodSelectionCommandServiceTest {

    @Mock
    private ChonkangsAccessValidator chonkangsAccessValidator;

    @Mock
    private ChonkangsRepository chonkangsRepository;

    @Mock
    private ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MoodCardResolver moodCardResolver;

    @Mock
    private ChonkangsMoodResultService chonkangsMoodResultService;

    @InjectMocks
    private ChonkangsMoodSelectionCommandService chonkangsMoodSelectionCommandService;

    @Test
    @DisplayName("무드 선택을 저장한 뒤 전원 완료 여부 판단 로직을 호출한다")
    void submitTriggersMoodResultConfirmationAfterSaving() {
        Long chonkangId = 1L;
        Long userId = 10L;
        Chonkangs chonkang = chonkang(chonkangId);
        User user = user(userId);
        List<MoodCard> moodCards = List.of(moodCard(1L), moodCard(2L), moodCard(3L));
        Set<Long> selectedMoodCardIds = Set.of(1L, 2L, 3L);

        when(chonkangsRepository.findById(chonkangId)).thenReturn(java.util.Optional.of(chonkang));
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(moodCardResolver.resolveExactlyThree(anySet())).thenReturn(moodCards);

        chonkangsMoodSelectionCommandService.submit(chonkangId, userId,
                new SubmitMoodSelectionRequest(selectedMoodCardIds));

        verify(chonkangsAccessValidator).validateMember(chonkangId, userId);
        verify(chonkangsMoodSelectionRepository).deleteAllByChonkangIdAndUserId(chonkangId, userId);
        verify(chonkangsMoodSelectionRepository, org.mockito.Mockito.times(3)).save(any());
        verify(chonkangsMoodResultService).confirmIfAllMembersSubmitted(chonkang);
    }

    private Chonkangs chonkang(Long id) {
        Chonkangs chonkang = Chonkangs.builder()
                .name("제주 촌캉스")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 3))
                .plannedMemberCount(3)
                .companionType(CompanionType.FRIEND)
                .travelMethod(TravelMethod.CAR)
                .desiredRegion(Region.JEJU)
                .accommodationConditions(Set.of())
                .inviteCode("INVITE" + id)
                .host(user(100L))
                .build();
        ReflectionTestUtils.setField(chonkang, "id", id);
        return chonkang;
    }

    private User user(Long id) {
        User user = User.builder()
                .provider(AuthProvider.KAKAO)
                .providerId("provider-" + id)
                .nickname("user" + id)
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private MoodCard moodCard(Long id) {
        MoodCard card = MoodCard.builder()
                .imageUrl("https://example.com/card.png")
                .accommodationType(accommodationType(id))
                .tags(Set.of())
                .build();
        ReflectionTestUtils.setField(card, "id", id);
        return card;
    }

    private AccommodationType accommodationType(Long id) {
        AccommodationType type = AccommodationType.builder().name("펜션" + id).build();
        ReflectionTestUtils.setField(type, "id", id);
        return type;
    }
}
