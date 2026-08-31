package com.example.moodchon.domain.mood;

import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.mood.entity.MoodTagCategory;
import com.example.moodchon.domain.mood.entity.MoodType;
import com.example.moodchon.domain.mood.repository.MoodTagRepository;
import com.example.moodchon.domain.mood.repository.MoodTypeRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// "태그 체계 및 사용자 무드 유형" 문서의 무드 도출용 태그(22개)/공통 무드 유형(7개) 시드 데이터.
// 앱 기동 시 비어 있으면 채우고, 이미 있으면 아무 것도 하지 않는다.
@Component
@RequiredArgsConstructor
public class MoodTagSeeder implements ApplicationRunner {

    private final MoodTagRepository moodTagRepository;
    private final MoodTypeRepository moodTypeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, MoodTag> tagsByName = seedMoodTags();
        seedMoodTypes(tagsByName);
    }

    private Map<String, MoodTag> seedMoodTags() {
        if (moodTagRepository.count() > 0) {
            return moodTagRepository.findAll().stream()
                    .collect(Collectors.toMap(MoodTag::getName, Function.identity()));
        }

        List<MoodTag> tags = new ArrayList<>();
        tags.addAll(createTags(MoodTagCategory.ACCOMMODATION, "한옥", "시골집", "민박/펜션", "글램핑"));
        tags.addAll(createTags(MoodTagCategory.PLACE, "숲속", "해안가", "강/호수", "논/밭", "골목길", "마당", "장터"));
        tags.addAll(createTags(MoodTagCategory.SCENE, "산책", "노을", "밤하늘", "체험"));
        tags.addAll(createTags(MoodTagCategory.ATMOSPHERE,
                "조용한", "아늑한", "고즈넉한", "여유로운", "활기있는", "빈티지", "밤감성"));

        List<MoodTag> saved = moodTagRepository.saveAll(tags);
        return saved.stream().collect(Collectors.toMap(MoodTag::getName, Function.identity()));
    }

    private List<MoodTag> createTags(MoodTagCategory category, String... names) {
        return Arrays.stream(names)
                .map(name -> MoodTag.builder().name(name).category(category).build())
                .toList();
    }

    private void seedMoodTypes(Map<String, MoodTag> tagsByName) {
        if (moodTypeRepository.count() > 0) {
            return;
        }

        moodTypeRepository.saveAll(List.of(
                moodType(tagsByName, "고즈넉한 쉼표 무드",
                        "오래된 골목과 조용한 공간에서 천천히 걸음을 늦추고, 바쁜 일상에서 잠시 벗어나 차분하게 쉬어가는 무드예요.",
                        "고즈넉한", "조용한", "골목길", "한옥", "마당"),
                moodType(tagsByName, "정겨운 시골살이 무드",
                        "마당과 논밭이 펼쳐진 시골 풍경 속에서 소박한 일상을 함께하며, 여유롭고 정겨운 시간을 보내는 무드예요.",
                        "마당", "시골집", "논/밭", "여유로운"),
                moodType(tagsByName, "숲속 산책 무드",
                        "푸른 숲과 자연 속을 함께 천천히 걸으며, 맑은 공기를 느끼고 편안하게 쉬어가는 무드예요.",
                        "숲속", "산책", "여유로운", "민박/펜션", "조용한"),
                moodType(tagsByName, "해안가 노을 무드",
                        "바다나 강, 호수 같은 물가에서 산책을 즐기고, 저물어가는 노을을 바라보며 여유를 느끼는 무드예요.",
                        "해안가", "강/호수", "노을", "산책", "여유로운"),
                moodType(tagsByName, "별빛 야외 무드",
                        "별빛이 보이는 마당이나 야외 공간에 함께 모여 밤공기를 느끼고, 이야기하며 감성적인 시간을 보내는 무드예요.",
                        "밤하늘", "밤감성", "여유로운", "글램핑", "마당"),
                moodType(tagsByName, "빈티지 기록 무드",
                        "오래된 골목과 레트로한 공간을 천천히 둘러보며, 마음에 드는 장면을 사진으로 남겨 기록하는 무드예요.",
                        "빈티지", "골목길", "고즈넉한", "시골집", "아늑한"),
                moodType(tagsByName, "로컬 체험 무드",
                        "지역의 장터와 축제, 체험을 직접 즐기며 새로운 문화를 만나고, 로컬의 활기와 정취를 가까이 느끼는 무드예요.",
                        "장터", "체험", "활기있는")
        ));
    }

    private MoodType moodType(Map<String, MoodTag> tagsByName, String name, String description,
                               String... coreTagNames) {
        Set<MoodTag> coreTags = Arrays.stream(coreTagNames)
                .map(tagsByName::get)
                .collect(Collectors.toSet());
        return MoodType.builder().name(name).description(description).coreTags(coreTags).build();
    }
}
