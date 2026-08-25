package com.example.moodchon.domain.chonkangs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMoodSelection;
import com.example.moodchon.domain.chonkangs.entity.CompanionType;
import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.domain.chonkangs.entity.TravelMethod;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.mood.entity.AccommodationType;
import com.example.moodchon.domain.mood.entity.MoodCard;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.user.entity.AuthProvider;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.entity.UserRole;
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
class ChonkangsMoodResultServiceTest {

    @Mock
    private ChonkangsMemberRepository chonkangsMemberRepository;

    @Mock
    private ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;

    @InjectMocks
    private ChonkangsMoodResultService chonkangsMoodResultService;

    @Test
    @DisplayName("아직 제출하지 않은 구성원이 있으면 무드를 확정하지 않는다")
    void doesNotConfirmMoodWhenSomeMembersHaveNotSubmitted() {
        Chonkangs chonkang = chonkang(1L);
        User submitted = user(1L);
        MoodCard moodCard = moodCard(1L, moodTag(1L, "고요함"));

        when(chonkangsMoodSelectionRepository.findAllByChonkangId(1L))
                .thenReturn(List.of(moodSelection(chonkang, submitted, moodCard)));
        when(chonkangsMemberRepository.countByChonkangId(1L)).thenReturn(3L);

        chonkangsMoodResultService.confirmIfAllMembersSubmitted(chonkang);

        assertThat(chonkang.isMoodDecided()).isFalse();
    }

    @Test
    @DisplayName("전원 제출을 완료하면 최다 득표 태그로 무드를 확정한다")
    void confirmsMoodWithMostVotedTagWhenAllMembersSubmitted() {
        Chonkangs chonkang = chonkang(1L);
        User userA = user(1L);
        User userB = user(2L);
        MoodTag popularTag = moodTag(1L, "고요함");
        MoodTag minorTag = moodTag(2L, "왁자지껄");
        MoodCard cardWithPopularTag = moodCard(1L, popularTag);
        MoodCard cardWithBothTags = moodCard(2L, popularTag, minorTag);

        when(chonkangsMoodSelectionRepository.findAllByChonkangId(1L)).thenReturn(List.of(
                moodSelection(chonkang, userA, cardWithPopularTag),
                moodSelection(chonkang, userB, cardWithBothTags)
        ));
        when(chonkangsMemberRepository.countByChonkangId(1L)).thenReturn(2L);

        chonkangsMoodResultService.confirmIfAllMembersSubmitted(chonkang);

        assertThat(chonkang.isMoodDecided()).isTrue();
        assertThat(chonkang.getMoodName()).isEqualTo("고요함");
        assertThat(chonkang.getMoodDescription()).contains("고요함");
    }

    @Test
    @DisplayName("득표수가 같으면 id가 더 낮은 태그를 우선한다")
    void picksLowerIdTagOnTie() {
        Chonkangs chonkang = chonkang(1L);
        User user = user(1L);
        MoodTag lowerIdTag = moodTag(1L, "고요함");
        MoodTag higherIdTag = moodTag(2L, "왁자지껄");
        MoodCard card = moodCard(1L, lowerIdTag, higherIdTag);

        when(chonkangsMoodSelectionRepository.findAllByChonkangId(1L))
                .thenReturn(List.of(moodSelection(chonkang, user, card)));
        when(chonkangsMemberRepository.countByChonkangId(1L)).thenReturn(1L);

        chonkangsMoodResultService.confirmIfAllMembersSubmitted(chonkang);

        assertThat(chonkang.getMoodName()).isEqualTo("고요함");
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

    private MoodTag moodTag(Long id, String name) {
        MoodTag tag = MoodTag.builder().name(name).build();
        ReflectionTestUtils.setField(tag, "id", id);
        return tag;
    }

    private MoodCard moodCard(Long id, MoodTag... tags) {
        MoodCard card = MoodCard.builder()
                .imageUrl("https://example.com/card.png")
                .accommodationType(accommodationType(id))
                .tags(Set.of(tags))
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
