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
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.mood.entity.MoodTagCategory;
import com.example.moodchon.domain.mood.entity.MoodType;
import com.example.moodchon.domain.mood.repository.MoodTypeRepository;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.entity.Post;
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

    @Mock
    private MoodTypeRepository moodTypeRepository;

    @InjectMocks
    private ChonkangsMoodResultService chonkangsMoodResultService;

    @Test
    @DisplayName("아직 제출하지 않은 구성원이 있으면 무드를 확정하지 않는다")
    void doesNotConfirmMoodWhenSomeMembersHaveNotSubmitted() {
        Chonkangs chonkang = chonkang(1L);
        User submitted = user(1L);
        Post post = post(1L, moodTag(1L, "고즈넉한"));

        when(chonkangsMoodSelectionRepository.findAllByChonkangId(1L))
                .thenReturn(List.of(moodSelection(chonkang, submitted, post)));
        when(chonkangsMemberRepository.countByChonkangId(1L)).thenReturn(3L);

        chonkangsMoodResultService.confirmIfAllMembersSubmitted(chonkang);

        assertThat(chonkang.isMoodDecided()).isFalse();
    }

    @Test
    @DisplayName("전원 제출을 완료하면 태그와 가장 많이 겹치는 무드 유형으로 확정한다")
    void confirmsMoodWithBestMatchingMoodTypeWhenAllMembersSubmitted() {
        Chonkangs chonkang = chonkang(1L);
        User userA = user(1L);
        User userB = user(2L);
        MoodTag quiet = moodTag(1L, "조용한");
        MoodTag alley = moodTag(2L, "골목길");
        MoodTag lively = moodTag(3L, "활기있는");
        Post postWithQuietAndAlley = post(1L, quiet, alley);
        Post postWithLively = post(2L, lively);

        MoodType quietAlleyType = moodType(1L, "고즈넉한 쉼표 무드", "설명1", quiet, alley);
        MoodType livelyType = moodType(2L, "로컬 체험 무드", "설명2", lively);

        when(chonkangsMoodSelectionRepository.findAllByChonkangId(1L)).thenReturn(List.of(
                moodSelection(chonkang, userA, postWithQuietAndAlley),
                moodSelection(chonkang, userB, postWithLively)
        ));
        when(chonkangsMemberRepository.countByChonkangId(1L)).thenReturn(2L);
        when(moodTypeRepository.findAllWithCoreTags()).thenReturn(List.of(quietAlleyType, livelyType));

        chonkangsMoodResultService.confirmIfAllMembersSubmitted(chonkang);

        assertThat(chonkang.isMoodDecided()).isTrue();
        assertThat(chonkang.getMoodName()).isEqualTo("고즈넉한 쉼표 무드");
        assertThat(chonkang.getMoodDescription()).isEqualTo("설명1");
    }

    @Test
    @DisplayName("점수가 같으면 id가 더 낮은 무드 유형을 우선한다")
    void picksLowerIdMoodTypeOnTie() {
        Chonkangs chonkang = chonkang(1L);
        User user = user(1L);
        MoodTag quiet = moodTag(1L, "조용한");
        Post post = post(1L, quiet);

        MoodType lowerIdType = moodType(1L, "무드A", "설명A", quiet);
        MoodType higherIdType = moodType(2L, "무드B", "설명B", quiet);

        when(chonkangsMoodSelectionRepository.findAllByChonkangId(1L))
                .thenReturn(List.of(moodSelection(chonkang, user, post)));
        when(chonkangsMemberRepository.countByChonkangId(1L)).thenReturn(1L);
        when(moodTypeRepository.findAllWithCoreTags()).thenReturn(List.of(higherIdType, lowerIdType));

        chonkangsMoodResultService.confirmIfAllMembersSubmitted(chonkang);

        assertThat(chonkang.getMoodName()).isEqualTo("무드A");
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
        MoodTag tag = MoodTag.builder().name(name).category(MoodTagCategory.ATMOSPHERE).build();
        ReflectionTestUtils.setField(tag, "id", id);
        return tag;
    }

    // 무드 카드는 탐색 탭 게시물이다.
    private Post post(Long id, MoodTag... tags) {
        Post post = Post.builder()
                .place(place(id))
                .imageUrl("https://example.com/post.png")
                .tags(Set.of(tags))
                .build();
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private Place place(Long id) {
        Place place = Place.builder()
                .externalContentId("content-" + id)
                .name("장소" + id)
                .category(PlaceCategory.ACCOMMODATION)
                .build();
        ReflectionTestUtils.setField(place, "id", id);
        return place;
    }

    private MoodType moodType(Long id, String name, String description, MoodTag... coreTags) {
        MoodType moodType = MoodType.builder()
                .name(name)
                .description(description)
                .coreTags(Set.of(coreTags))
                .build();
        ReflectionTestUtils.setField(moodType, "id", id);
        return moodType;
    }

    private ChonkangsMoodSelection moodSelection(Chonkangs chonkang, User user, Post post) {
        return ChonkangsMoodSelection.builder()
                .chonkang(chonkang)
                .user(user)
                .post(post)
                .build();
    }
}
