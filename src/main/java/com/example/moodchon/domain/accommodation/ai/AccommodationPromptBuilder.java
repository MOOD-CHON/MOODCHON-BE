package com.example.moodchon.domain.accommodation.ai;

import com.example.moodchon.domain.accommodation.external.TourApiLodgingIntroFields;
import com.example.moodchon.domain.chonkangs.entity.AccommodationCondition;
import com.example.moodchon.domain.place.entity.Place;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class AccommodationPromptBuilder {

    private static final int DESCRIPTION_MAX_LENGTH = 200;

    private AccommodationPromptBuilder() {
    }

    static String buildSystemPrompt() {
        return """
                너는 숙소 추천 큐레이터야. 주어진 무드와 숙소 후보 목록을 바탕으로
                각 숙소가 무드와 얼마나 어울리는지 평가해.
                규칙:
                - placeId는 반드시 후보 숙소 목록에 있는 값만 사용한다.
                - 후보 숙소 전체에 대해 각각 하나씩 결과를 만든다.
                - matchScore는 0~100 사이 정수로, 이 숙소가 주어진 무드와 얼마나 어울리는지를 평가한다.
                  인원수를 충분히 수용하지 못하거나 희망 조건(바베큐/취사/반려동물 동반)을 만족하지 못하면 감점한다.
                  단, 정보없음으로 표시된 항목은 만족 여부를 알 수 없으니 감점 근거로 쓰지 않는다.
                - tags는 이 숙소를 설명하는 짧은 키워드를 1~3개 한국어로 제시한다.
                - highlights는 이 숙소가 무드와 잘 맞는 이유를 완결된 문장으로 1~3개 쓴다.
                  각 문장은 25자 이상 60자 이하로, 위치·시설·분위기 같은 구체적인 특징을 담아 설명한다.
                  "풀빌라", "자연경관"처럼 단어나 명사구만 나열하지 않는다.
                  인원수/바베큐/취사 조건이 맞거나 안 맞는 경우 그 사실도 반영해서 설명한다.
                - regrets는 이 숙소를 고를 때 아쉬울 수 있는 점을 1~3개의 짧은 문장으로 자연스러운
                  한국어로 설명한다. 잘 맞는 숙소라도 반드시 1개 이상 쓴다. 무드와 덜 맞는 부분,
                  희망 조건 충족이 애매한 부분, 인원수·위치·편의시설 면에서 감안할 점을 찾아 쓴다.
                  "정보가 없어 알 수 없다"처럼 데이터 부족을 지적하는 표현은 쓰지 않는다.
                - 응답은 지정된 JSON 스키마만 따르고 그 외 텍스트는 포함하지 않는다.
                """;
    }

    static String buildUserPrompt(AccommodationMatchContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("무드: ").append(context.moodName()).append('\n');
        prompt.append("무드 설명: ").append(context.moodDescription()).append("\n\n");
        prompt.append("인원수: ").append(context.plannedMemberCount()).append("명\n");
        prompt.append("희망 조건: ").append(describeConditions(context.accommodationConditions())).append("\n\n");

        prompt.append("후보 숙소 목록:\n");
        for (AccommodationCandidate candidate : context.candidates()) {
            Place place = candidate.place();
            TourApiLodgingIntroFields intro = candidate.lodgingIntro();

            prompt.append("- id=").append(place.getId())
                    .append(", name=").append(place.getName())
                    .append(", address=").append(place.getAddress())
                    .append(", description=").append(truncateDescription(place.getDescription()))
                    .append(", 바베큐=").append(describeFlag(intro.barbecue()))
                    .append(", 취사=").append(describeText(intro.chkCooking()))
                    .append(", 반려동물 동반=").append(describeText(intro.petAccompanyType()))
                    .append(", 수용인원=").append(describeText(intro.accomCountLodging()))
                    .append('\n');
        }

        return prompt.toString();
    }

    private static String describeConditions(Set<AccommodationCondition> conditions) {
        if (conditions == null) {
            return "특별한 조건 없음";
        }

        String joined = conditions.stream()
                .map(AccommodationPromptBuilder::describeCondition)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));

        return joined.isEmpty() ? "특별한 조건 없음" : joined;
    }

    private static String describeCondition(AccommodationCondition condition) {
        return switch (condition) {
            case BARBECUE -> "바베큐 가능 숙소 선호";
            case COOKING -> "취사 가능 숙소 선호";
            case PET_FRIENDLY -> "반려동물 동반 가능 숙소 선호";
        };
    }

    private static String describeFlag(String rawValue) {
        if ("1".equals(rawValue)) {
            return "가능";
        }
        if ("0".equals(rawValue)) {
            return "불가능";
        }
        return "정보없음";
    }

    private static String describeText(String rawValue) {
        return (rawValue == null || rawValue.isBlank()) ? "정보없음" : rawValue;
    }

    private static String truncateDescription(String description) {
        if (description == null || description.isBlank()) {
            return "정보없음";
        }
        return description.length() > DESCRIPTION_MAX_LENGTH
                ? description.substring(0, DESCRIPTION_MAX_LENGTH) + "..."
                : description;
    }
}
