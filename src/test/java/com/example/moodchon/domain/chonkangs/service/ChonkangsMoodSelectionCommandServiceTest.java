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
import com.example.moodchon.domain.mood.service.MoodCardResolver;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.entity.Post;
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
import org.mockito.InOrder;
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
        List<Post> selectedPosts = List.of(post(1L), post(2L), post(3L));
        Set<Long> selectedMoodCardIds = Set.of(1L, 2L, 3L);

        when(chonkangsRepository.findById(chonkangId)).thenReturn(java.util.Optional.of(chonkang));
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(moodCardResolver.resolveExactlyThree(anySet())).thenReturn(selectedPosts);

        chonkangsMoodSelectionCommandService.submit(chonkangId, userId,
                new SubmitMoodSelectionRequest(selectedMoodCardIds));

        verify(chonkangsAccessValidator).validateMember(chonkangId, userId);
        verify(chonkangsMoodSelectionRepository, org.mockito.Mockito.times(3)).save(any());
        verify(chonkangsMoodResultService).confirmIfAllMembersSubmitted(chonkang, userId);

        // 삭제를 flush 로 먼저 반영해야 같은 사진을 다시 고른 재제출이 유니크 제약에 걸리지 않는다.
        InOrder inOrder = org.mockito.Mockito.inOrder(chonkangsMoodSelectionRepository);
        inOrder.verify(chonkangsMoodSelectionRepository).deleteAllByChonkangIdAndUserId(chonkangId, userId);
        inOrder.verify(chonkangsMoodSelectionRepository).flush();
        inOrder.verify(chonkangsMoodSelectionRepository, org.mockito.Mockito.times(3)).save(any());
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

    // 무드 카드는 탐색 탭 게시물이다.
    private Post post(Long id) {
        Post post = Post.builder()
                .place(place(id))
                .imageUrl("https://example.com/post.png")
                .tags(Set.of())
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
}
