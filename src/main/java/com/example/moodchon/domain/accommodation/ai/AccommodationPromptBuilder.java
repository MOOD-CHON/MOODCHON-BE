package com.example.moodchon.domain.accommodation.ai;

import com.example.moodchon.domain.place.entity.Place;

final class AccommodationPromptBuilder {

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
                - tags는 이 숙소를 설명하는 짧은 키워드를 1~3개 한국어로 제시한다.
                - highlights는 이 숙소가 무드와 잘 맞는 이유를 1~3개의 짧은 문장으로 자연스러운 한국어로 설명한다.
                - 응답은 지정된 JSON 스키마만 따르고 그 외 텍스트는 포함하지 않는다.
                """;
    }

    static String buildUserPrompt(AccommodationMatchContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("무드: ").append(context.moodName()).append('\n');
        prompt.append("무드 설명: ").append(context.moodDescription()).append("\n\n");

        prompt.append("후보 숙소 목록:\n");
        for (Place place : context.candidates()) {
            prompt.append("- id=").append(place.getId())
                    .append(", name=").append(place.getName())
                    .append(", address=").append(place.getAddress())
                    .append('\n');
        }

        return prompt.toString();
    }
}
