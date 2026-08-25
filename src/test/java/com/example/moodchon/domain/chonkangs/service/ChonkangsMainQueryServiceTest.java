package com.example.moodchon.domain.chonkangs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.moodchon.domain.chonkangs.dto.response.ChonkangMainResponse;
import com.example.moodchon.domain.chonkangs.dto.response.MemberMoodProgressResponse;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMainStatus;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMember;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMoodSelection;
import com.example.moodchon.domain.chonkangs.entity.CompanionType;
import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.domain.chonkangs.entity.TravelMethod;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.mood.entity.AccommodationType;
import com.example.moodchon.domain.mood.entity.MoodCard;
import com.example.moodchon.domain.user.entity.AuthProvider;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.entity.UserRole;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChonkangsMainQueryServiceTest {

    @Mock
    private ChonkangsAccessValidator chonkangsAccessValidator;

    @Mock
    private ChonkangsRepository chonkangsRepository;

    @Mock
    private ChonkangsMemberRepository chonkangsMemberRepository;

    @Mock
    private ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;

    @InjectMocks
    private ChonkangsMainQueryService chonkangsMainQueryService;

    @Test
    @DisplayName("무드가 아직 결정되지 않았으면 구성원별 무드 선택 진행 상황을 반환한다")
    void returnsMoodProgressWhenMoodNotDecided() {
        Long chonkangId = 1L;
        Chonkangs chonkang = chonkang(chonkangId);
        User submittedUser = user(1L);
        User pendingUser = user(2L);
        ChonkangsMember submittedMember = member(chonkang, submittedUser);
        ChonkangsMember pendingMember = member(chonkang, pendingUser);
        MoodCard moodCard = moodCard(1L);

        when(chonkangsRepository.findById(chonkangId)).thenReturn(Optional.of(chonkang));
        when(chonkangsMemberRepository.findAllWithUserByChonkangId(chonkangId))
                .thenReturn(List.of(submittedMember, pendingMember));
        when(chonkangsMoodSelectionRepository.findAllByChonkangId(chonkangId))
                .thenReturn(List.of(moodSelection(chonkang, submittedUser, moodCard)));

        ChonkangMainResponse response = chonkangsMainQueryService.getMain(chonkangId, submittedUser.getId());

        assertThat(response.status()).isEqualTo(ChonkangsMainStatus.MOOD_VOTING);
        assertThat(response.moodResult()).isNull();
        assertThat(response.moodProgress().totalMemberCount()).isEqualTo(2);
        assertThat(response.moodProgress().completedMemberCount()).isEqualTo(1);
        assertThat(response.moodProgress().members())
                .extracting(MemberMoodProgressResponse::userId, MemberMoodProgressResponse::moodSelected)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(submittedUser.getId(), true),
                        org.assertj.core.groups.Tuple.tuple(pendingUser.getId(), false)
                );
    }

    @Test
    @DisplayName("무드가 결정되었으면 무드 결과를 반환하고 구성원 진행 상황은 조회하지 않는다")
    void returnsMoodResultWhenMoodDecided() {
        Long chonkangId = 1L;
        Chonkangs chonkang = chonkang(chonkangId);
        chonkang.confirmMood("고요함", "우리 팀이 가장 많이 선택한 무드는 '고요함'예요.");

        when(chonkangsRepository.findById(chonkangId)).thenReturn(Optional.of(chonkang));

        ChonkangMainResponse response = chonkangsMainQueryService.getMain(chonkangId, 1L);

        assertThat(response.status()).isEqualTo(ChonkangsMainStatus.MOOD_DECIDED);
        assertThat(response.moodProgress()).isNull();
        assertThat(response.moodResult().name()).isEqualTo("고요함");
        assertThat(response.moodResult().description()).contains("고요함");
        verify(chonkangsMemberRepository, never()).findAllWithUserByChonkangId(chonkangId);
        verify(chonkangsMoodSelectionRepository, never()).findAllByChonkangId(chonkangId);
    }

    private Chonkangs chonkang(Long id) {
        Chonkangs chonkang = Chonkangs.builder()
                .name("제주 촌캉스")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 3))
                .plannedMemberCount(2)
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

    private ChonkangsMember member(Chonkangs chonkang, User user) {
        return ChonkangsMember.builder()
                .chonkang(chonkang)
                .user(user)
                .build();
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

    private ChonkangsMoodSelection moodSelection(Chonkangs chonkang, User user, MoodCard moodCard) {
        return ChonkangsMoodSelection.builder()
                .chonkang(chonkang)
                .user(user)
                .moodCard(moodCard)
                .build();
    }
}
