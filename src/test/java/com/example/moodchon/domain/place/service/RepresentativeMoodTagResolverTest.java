package com.example.moodchon.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.mood.entity.MoodTagCategory;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.place.repository.PostRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RepresentativeMoodTagResolverTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private RepresentativeMoodTagResolver resolver;

    private final MoodTag quiet = tag(16L, "조용한", MoodTagCategory.ATMOSPHERE);
    private final MoodTag cozy = tag(17L, "아늑한", MoodTagCategory.ATMOSPHERE);
    private final MoodTag vintage = tag(21L, "빈티지", MoodTagCategory.ATMOSPHERE);
    private final MoodTag coast = tag(6L, "해안가", MoodTagCategory.PLACE);

    @Test
    @DisplayName("분위기 태그 중 그 태그를 가진 장소가 가장 적은 것을 대표로 고른다")
    void picksTheRarestAtmosphereTag() {
        Map<Long, Long> placeCounts = Map.of(16L, 28L, 17L, 63L, 21L, 12L);

        assertThat(RepresentativeMoodTagResolver.pick(Set.of(quiet, cozy, vintage), placeCounts))
                .contains(vintage);
        assertThat(RepresentativeMoodTagResolver.pick(Set.of(quiet, cozy), placeCounts))
                .contains(quiet);
    }

    @Test
    @DisplayName("분위기 태그가 아닌 태그(장소/장면/숙소)는 대표가 될 수 없다")
    void ignoresNonAtmosphereTags() {
        assertThat(RepresentativeMoodTagResolver.pick(Set.of(coast, cozy), Map.of(6L, 1L, 17L, 63L)))
                .contains(cozy);
        assertThat(RepresentativeMoodTagResolver.pick(Set.of(coast), Map.of(6L, 1L))).isEmpty();
    }

    @Test
    @DisplayName("장소 수가 같으면 id가 작은 태그를 고른다")
    void breaksTiesByLowerId() {
        assertThat(RepresentativeMoodTagResolver.pick(Set.of(quiet, cozy), Map.of(16L, 5L, 17L, 5L)))
                .contains(quiet);
    }

    @Test
    @DisplayName("같은 장소의 게시물은 모두 같은 대표 태그를 갖고, 태그는 하나뿐이다")
    void samePlacePostsShareOneRepresentativeTag() {
        when(postRepository.countPlacesByAtmosphereTag())
                .thenReturn(List.<Object[]>of(new Object[] {16L, 28L}, new Object[] {17L, 63L}));

        Post first = post(1L, place(10L), quiet, cozy, coast);
        Post second = post(2L, place(10L), quiet, cozy, coast);
        Post other = post(3L, place(11L), cozy);

        Map<Long, MoodTag> representative = resolver.representativeByPlace(List.of(first, second, other));

        assertThat(representative).hasSize(2);
        assertThat(representative.get(10L)).isSameAs(quiet);
        assertThat(representative.get(11L)).isSameAs(cozy);
    }

    @Test
    @DisplayName("분위기 태그가 없는 장소는 대표 태그가 없다")
    void placeWithoutAtmosphereTagHasNoRepresentative() {
        when(postRepository.countPlacesByAtmosphereTag()).thenReturn(List.of());

        Map<Long, MoodTag> representative = resolver.representativeByPlace(List.of(post(1L, place(10L), coast)));

        assertThat(representative).isEmpty();
    }

    private MoodTag tag(Long id, String name, MoodTagCategory category) {
        MoodTag tag = MoodTag.builder().name(name).category(category).build();
        ReflectionTestUtils.setField(tag, "id", id);
        return tag;
    }

    private Place place(Long id) {
        Place place = Place.builder()
                .externalContentId("content-" + id)
                .name("장소" + id)
                .category(PlaceCategory.TOURIST_SPOT)
                .build();
        ReflectionTestUtils.setField(place, "id", id);
        return place;
    }

    private Post post(Long id, Place place, MoodTag... tags) {
        Post post = Post.builder()
                .place(place)
                .imageUrl("https://example.com/" + id + ".jpg")
                .tags(Set.of(tags))
                .build();
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
