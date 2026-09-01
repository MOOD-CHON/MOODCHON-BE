package com.example.moodchon.domain.place.ai;

final class PostTagPromptBuilder {

    private PostTagPromptBuilder() {
    }

    static String buildSystemPrompt() {
        return """
                너는 여행지 게시물에 분위기 태그를 붙이는 큐레이터야. 주어진 장소 정보를 보고
                아래 후보 태그 목록 중에서 이 장소와 어울리는 태그를 2~4개 선택해.
                규칙:
                - 반드시 후보 태그 목록에 있는 이름만 그대로 사용한다 (새로운 태그를 만들지 않는다).
                - 장소의 카테고리, 이름, 설명에서 드러나는 분위기와 특징을 기준으로 판단한다.
                - 확실히 어울리는 태그가 없으면 빈 배열을 반환한다.
                - 응답은 지정된 JSON 스키마만 따르고 그 외 텍스트는 포함하지 않는다.
                """;
    }

    static String buildUserPrompt(PostTagContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("장소 이름: ").append(context.placeName()).append('\n');
        prompt.append("카테고리: ").append(context.category().getLabel()).append('\n');
        prompt.append("설명: ").append(context.description() == null || context.description().isBlank()
                ? "(설명 없음)" : context.description()).append("\n\n");

        prompt.append("후보 태그 목록: ").append(String.join(", ", context.availableTagNames()));

        return prompt.toString();
    }
}
