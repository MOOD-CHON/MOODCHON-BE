package com.example.moodchon.domain.recommendation.ai;

import com.example.moodchon.domain.place.entity.Place;

final class ActivityScorePromptBuilder {

    private ActivityScorePromptBuilder() {
    }

    static String buildSystemPrompt() {
        return """
                너는 여행 일정 큐레이터야. 주어진 무드와 후보 장소 목록을 보고, 각 장소가 이 무드와
                얼마나 어울리는지 평가해.
                규칙:
                - 후보 장소 목록에 있는 placeId만 빠짐없이 하나씩 채점한다.
                - moodFitScore는 0~100 사이 정수로, 이 장소가 주어진 무드와 얼마나 어울리는지를 평가한다.
                - aiSummary는 한 문장으로, 왜 이 장소가 이 무드에 어울리는지 자연스러운 한국어로 설명한다.
                - 응답은 지정된 JSON 스키마만 따르고 그 외 텍스트는 포함하지 않는다.
                """;
    }

    static String buildUserPrompt(ActivityScoreContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("무드: ").append(context.moodName()).append('\n');
        prompt.append("무드 설명: ").append(context.moodDescription()).append("\n\n");

        prompt.append("후보 장소 목록:\n");
        for (Place place : context.candidates()) {
            prompt.append("- id=").append(place.getId())
                    .append(", name=").append(place.getName())
                    .append(", category=").append(place.getCategory().getLabel())
                    .append(", address=").append(place.getAddress())
                    .append('\n');
        }

        return prompt.toString();
    }
}
